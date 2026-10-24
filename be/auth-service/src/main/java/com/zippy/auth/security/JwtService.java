package com.zippy.auth.security;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.zippy.auth.config.AuthProperties;
import com.zippy.auth.constant.SecurityConstants;
import com.zippy.auth.domain.entity.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import javax.crypto.spec.SecretKeySpec;
import java.time.*;
import java.util.*;
@Service
public class JwtService {
    private final AuthProperties properties;
    private final Clock clock;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    public JwtService(AuthProperties properties, Clock clock) {
        this.properties = properties; this.clock = clock;
        byte[] key;
        try { key = Base64.getDecoder().decode(properties.jwtSecretBase64()); }
        catch (IllegalArgumentException ex) { throw new IllegalStateException("JWT_SECRET_BASE64 must be valid Base64"); }
        if (key.length < 32) throw new IllegalStateException("JWT_SECRET_BASE64 must decode to at least 32 bytes");
        var secret = new SecretKeySpec(key, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secret));
        var nimbus = NimbusJwtDecoder.withSecretKey(secret).macAlgorithm(MacAlgorithm.HS256).build();
        var timestamp = new JwtTimestampValidator(Duration.ZERO);
        timestamp.setClock(clock);
        nimbus.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
            timestamp, new JwtIssuerValidator(properties.jwtIssuer()),
            new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(SecurityConstants.JWT_AUDIENCE)),
            new JwtClaimValidator<String>("sub", sub -> sub != null && !sub.isBlank()),
            new JwtClaimValidator<Instant>("exp", Objects::nonNull)));
        decoder = nimbus;
    }
    public String create(User user, UUID sessionId) {
        Instant now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer(properties.jwtIssuer()).subject(user.getId().toString())
            .id(UUID.randomUUID().toString()).claim("pwdv", passwordVersion(user.getPassword()))
            .claim("sid", sessionId.toString())
            .audience(List.of(SecurityConstants.JWT_AUDIENCE)).issuedAt(now).expiresAt(now.plusSeconds(properties.accessExpirationSeconds()))
            .claim("roles", user.getRoles().stream().map(r -> r.getName().name()).sorted().toList()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
    public UUID subject(String token) { return UUID.fromString(decoder.decode(token).getSubject()); }
    public Jwt decode(String token) { return decoder.decode(token); }
    public UUID sessionId(Jwt token) {
        String sid = token.getClaimAsString("sid");
        if (sid == null) throw new IllegalArgumentException("Session missing; sign in again");
        return UUID.fromString(sid);
    }
    public boolean matchesPassword(Jwt token, String passwordHash) {
        String version = token.getClaimAsString("pwdv");
        return version != null && java.security.MessageDigest.isEqual(
            version.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
            passwordVersion(passwordHash).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }
    private String passwordVersion(String hash) {
        try {
            return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(hash.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 unavailable", ex); }
    }
}
