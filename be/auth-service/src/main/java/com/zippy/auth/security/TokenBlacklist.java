package com.zippy.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.HexFormat;

@Service @RequiredArgsConstructor
public class TokenBlacklist {
    private final StringRedisTemplate redis;
    private final Clock clock;
    public void revoke(String token, Instant expiresAt) {
        Duration remaining = Duration.between(clock.instant(), expiresAt);
        if (remaining.toMillis() > 0) redis.opsForValue().set(key(token), "1", remaining);
    }
    public boolean isRevoked(String token) { return Boolean.TRUE.equals(redis.hasKey(key(token))); }
    public void revokeRefreshHash(String hash, Instant expiresAt) {
        putUntil("auth:revoked:refresh:" + hash, expiresAt);
    }
    public boolean isRefreshRevoked(String hash) {
        return Boolean.TRUE.equals(redis.hasKey("auth:revoked:refresh:" + hash));
    }
    public void revokeSession(java.util.UUID sessionId, Instant expiresAt) {
        putUntil("auth:session:revoked:" + sessionId, expiresAt);
    }
    public boolean isSessionRevoked(java.util.UUID sessionId) {
        return Boolean.TRUE.equals(redis.hasKey("auth:session:revoked:" + sessionId));
    }
    private void putUntil(String key, Instant expiresAt) {
        Duration ttl = Duration.between(clock.instant(), expiresAt);
        if (ttl.toMillis() > 0) redis.opsForValue().set(key, "1", ttl);
    }
    private String key(String token) {
        try {
            return "auth:revoked:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 unavailable", ex); }
    }
}
