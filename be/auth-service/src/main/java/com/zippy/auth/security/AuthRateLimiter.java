package com.zippy.auth.security;

import com.zippy.auth.exception.AppException;
import com.zippy.auth.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

/** Shared fixed windows: counting and expiry are atomic across API instances. */
@Service @RequiredArgsConstructor
public class AuthRateLimiter {
    private final StringRedisTemplate redis;
    private static final DefaultRedisScript<Long> TAKE = new DefaultRedisScript<>("""
        local count = tonumber(redis.call('GET', KEYS[1]) or '0')
        if count >= tonumber(ARGV[1]) then return 0 end
        count = redis.call('INCR', KEYS[1])
        if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[2]) end
        return 1
        """, Long.class);

    public void check(String scope, String subject, int limit, Duration window) {
        String key = "auth:limit:" + scope + ":" + hash(subject);
        Long accepted = redis.execute(TAKE, List.of(key), Integer.toString(limit), Long.toString(window.toMillis()));
        if (accepted == null) throw new AppException(ErrorCode.AUTH_DEPENDENCY_UNAVAILABLE);
        if (accepted != 1) throw new AppException(ErrorCode.AUTH_RATE_LIMITED);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 unavailable", ex); }
    }
}
