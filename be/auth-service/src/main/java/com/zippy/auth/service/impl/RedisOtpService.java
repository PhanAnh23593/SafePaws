package com.zippy.auth.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zippy.auth.config.AuthProperties;
import com.zippy.auth.exception.*;
import com.zippy.auth.service.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class RedisOtpService implements OtpService {
    public static final Duration OTP_TTL = Duration.ofMinutes(3);
    public static final Duration COOLDOWN = Duration.ofSeconds(60);
    public static final Duration FLOW_TTL = Duration.ofMinutes(15);
    public static final Duration RESET_TTL = Duration.ofMinutes(5);
    private static final DefaultRedisScript<Long> CAS = new DefaultRedisScript<>("""
        if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end
        if ARGV[2] == '' then redis.call('DEL', KEYS[1])
        else redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3]) end
        return 1
        """, Long.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final MailService mail;
    private final Clock clock;
    private final byte[] secret;
    private final SecureRandom random = new SecureRandom();

    public RedisOtpService(StringRedisTemplate redis, ObjectMapper mapper, MailService mail,
            Clock clock, AuthProperties properties) {
        this.redis = redis; this.mapper = mapper; this.mail = mail; this.clock = clock;
        this.secret = Base64.getDecoder().decode(properties.jwtSecretBase64());
    }

    public record Flow(Purpose purpose, Registration registration, String otpHash,
                       long expiresAt, String status, String operation) {
        @Override public String toString() { return "Flow[redacted]"; }
    }

    @Override public String start(Purpose purpose, Registration registration) {
        String token = randomToken();
        String cooldownKey = cooldownKey(purpose, registration.email());
        acquireCooldown(cooldownKey, token);
        Flow sending = new Flow(purpose, registration, "", 0, "SENDING", token);
        String staged = json(sending);
        try {
            redis.opsForValue().set(key(token), staged, FLOW_TTL);
            deliver(token, sending, staged);
            return token;
        } catch (RuntimeException ex) {
            cleanup(key(token), staged, "");
            cleanup(cooldownKey, token, "");
            throw ex;
        }
    }

    @Override public void resend(String token, Purpose purpose) {
        String oldJson = read(token, purpose);
        Flow old = parse(oldJson);
        requireStatus(old, "PENDING");
        String operation = randomToken();
        String cooldownKey = cooldownKey(purpose, old.registration().email());
        acquireCooldown(cooldownKey, operation);
        Flow sending = new Flow(purpose, old.registration(), old.otpHash(), old.expiresAt(), "SENDING", operation);
        String staged = json(sending);
        if (!replace(key(token), oldJson, staged, FLOW_TTL)) {
            cleanup(cooldownKey, operation, "");
            throw new AppException(ErrorCode.AUTH_FLOW_INVALID);
        }
        try {
            deliver(token, sending, staged);
        } catch (RuntimeException ex) {
            cleanup(key(token), staged, oldJson);
            cleanup(cooldownKey, operation, "");
            throw ex;
        }
    }

    private void deliver(String token, Flow sending, String staged) {
        String code;
        do { code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000)); }
        while (digest(token, code).equals(sending.otpHash()));
        mail.sendOtp(sending.registration().email(), code, sending.purpose());
        Flow pending = new Flow(sending.purpose(), sending.registration(), digest(token, code),
            clock.millis() + OTP_TTL.toMillis(), "PENDING", sending.operation());
        if (!replace(key(token), staged, json(pending), FLOW_TTL))
            throw new AppException(ErrorCode.AUTH_FLOW_INVALID);
    }

    @Override public Claim claimRegistration(String token, String code) {
        return reserve(token, verifiedOtp(token, code, Purpose.REGISTER));
    }

    @Override public void verifyReset(String token, String code) {
        String previous = verifiedOtp(token, code, Purpose.RESET_PASSWORD);
        Flow flow = parse(previous);
        Flow verified = new Flow(flow.purpose(), flow.registration(), "",
            clock.millis() + RESET_TTL.toMillis(), "VERIFIED", randomToken());
        if (!replace(key(token), previous, json(verified), RESET_TTL))
            throw new AppException(ErrorCode.AUTH_FLOW_INVALID);
    }

    @Override public Claim claimReset(String token) {
        String previous = read(token, Purpose.RESET_PASSWORD);
        Flow flow = parse(previous);
        if (!"VERIFIED".equals(flow.status()) || clock.millis() >= flow.expiresAt())
            throw new AppException(ErrorCode.AUTH_RESET_SESSION_EXPIRED);
        return reserve(token, previous);
    }

    private Claim reserve(String token, String previous) {
        Flow flow = parse(previous);
        String reserved = json(new Flow(flow.purpose(), flow.registration(), flow.otpHash(),
            flow.expiresAt(), "PROCESSING", randomToken()));
        if (!replace(key(token), previous, reserved, FLOW_TTL))
            throw new AppException(ErrorCode.AUTH_FLOW_INVALID);
        return new Claim(token, previous, reserved, flow.registration());
    }

    @Override public void finish(Claim claim, boolean committed) {
        // Runs after DB completion. A process crash leaves PROCESSING, which fails closed.
        replace(key(claim.flowToken()), claim.reserved(), committed ? "" : claim.previous(), FLOW_TTL);
    }

    private String verifiedOtp(String token, String code, Purpose purpose) {
        String raw = read(token, purpose);
        Flow flow = parse(raw);
        requireStatus(flow, "PENDING");
        if (clock.millis() >= flow.expiresAt()) throw new AppException(ErrorCode.AUTH_OTP_EXPIRED);
        if (code == null || !MessageDigest.isEqual(flow.otpHash().getBytes(StandardCharsets.US_ASCII),
                digest(token, code).getBytes(StandardCharsets.US_ASCII)))
            throw new AppException(ErrorCode.AUTH_INVALID_OTP);
        return raw;
    }

    private String read(String token, Purpose purpose) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}"))
            throw new AppException(ErrorCode.AUTH_FLOW_INVALID);
        String value = redis.opsForValue().get(key(token));
        if (value == null || parse(value).purpose() != purpose) throw new AppException(ErrorCode.AUTH_FLOW_INVALID);
        return value;
    }
    private void requireStatus(Flow flow, String expected) {
        if (!expected.equals(flow.status())) throw new AppException(ErrorCode.AUTH_FLOW_INVALID);
    }
    private void acquireCooldown(String key, String owner) {
        if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, owner, COOLDOWN)))
            throw new AppException(ErrorCode.AUTH_OTP_COOLDOWN);
    }
    private boolean replace(String key, String expected, String replacement, Duration ttl) {
        return Long.valueOf(1).equals(redis.execute(CAS, List.of(key), expected, replacement, Long.toString(ttl.toMillis())));
    }
    private void cleanup(String key, String expected, String replacement) {
        try { replace(key, expected, replacement, FLOW_TTL); }
        catch (RuntimeException ignored) { /* SENDING is unusable; every key has bounded TTL. */ }
    }
    private String randomToken() {
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private String digest(String context, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal((context + ":" + value).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) { throw new IllegalStateException("HMAC unavailable", ex); }
    }
    private String key(String token) { return "auth:flow:" + digest("flow", token); }
    private String cooldownKey(Purpose purpose, String email) { return "auth:cooldown:" + purpose + ":" + digest("email", email); }
    private String json(Flow value) {
        try { return mapper.writeValueAsString(value); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Cannot encode auth flow"); }
    }
    private Flow parse(String value) {
        try { return mapper.readValue(value, Flow.class); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Cannot decode auth flow"); }
    }
}
