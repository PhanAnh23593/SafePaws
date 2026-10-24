package com.zippy.auth.service.impl;

import com.zippy.auth.config.AuthProperties;
import com.zippy.auth.domain.entity.*;
import com.zippy.auth.exception.*;
import com.zippy.auth.repository.RefreshTokenRepository;
import com.zippy.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Clock;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class TokenServiceImpl implements TokenService {
    private final RefreshTokenRepository tokens;
    private final com.zippy.auth.repository.UserRepository users;
    private final AuthProperties properties;
    private final Clock clock;
    private final com.zippy.auth.security.TokenBlacklist blacklist;
    private final SecureRandom random = new SecureRandom();

    @Override public IssuedRefreshToken issue(User user) {
        String expectedPassword = user.getPassword();
        User locked = users.findLockedById(user.getId()).orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
        if (!Objects.equals(users.findPasswordById(user.getId()).orElse(null), expectedPassword))
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        return create(locked, UUID.randomUUID());
    }
    private IssuedRefreshToken create(User user, UUID sessionId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var token = new RefreshToken();
        token.setUser(user);
        token.setSessionId(sessionId);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(clock.instant().plusSeconds(properties.refreshExpirationSeconds()));
        tokens.save(token);
        return new IssuedRefreshToken(raw, user, sessionId);
    }
    @Override @Transactional(noRollbackFor = RefreshTokenReuseException.class)
    public IssuedRefreshToken rotate(String rawToken) {
        validate(rawToken);
        UUID userId = tokens.findUserIdByHash(hash(rawToken))
            .orElseThrow(() -> new AppException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));
        User user = users.findLockedById(userId).orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
        RefreshToken token = tokens.findLockedByHash(hash(rawToken))
            .orElseThrow(() -> new AppException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));
        if (token.isRevoked()) {
            revokeSession(userId, token.getSessionId());
            throw new RefreshTokenReuseException();
        }
        if (!token.getExpiresAt().isAfter(clock.instant()))
            throw new AppException(ErrorCode.AUTH_EXPIRED_REFRESH_TOKEN);
        if (blacklist.isRefreshRevoked(token.getTokenHash()) || blacklist.isSessionRevoked(token.getSessionId()))
            throw new AppException(ErrorCode.AUTH_REVOKED_REFRESH_TOKEN);
        token.setRevoked(true);
        return create(user, token.getSessionId());
    }
    @Override public void revoke(String rawToken, UUID userId) {
        validate(rawToken);
        users.findLockedById(userId).orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
        tokens.findLockedByHash(hash(rawToken)).ifPresent(token -> {
            if (!token.getUser().getId().equals(userId)) throw new AppException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
            token.setRevoked(true);
        });
    }
    @Override public void revokeAll(UUID userId) { tokens.revokeAllForUser(userId); }
    @Override public void revokeSession(UUID userId, UUID sessionId) {
        // Same lock order as rotate: a concurrent refresh cannot escape this logout.
        users.findLockedById(userId).orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
        var sessionTokens = tokens.findByUserIdAndSessionId(userId, sessionId);
        if (sessionTokens.isEmpty()) throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        var until = clock.instant().plusSeconds(properties.accessExpirationSeconds());
        for (var token : sessionTokens) {
            if (token.getExpiresAt().isAfter(until)) until = token.getExpiresAt();
        }
        // Write the session marker first. Redis failures never report successful logout.
        blacklist.revokeSession(sessionId, until);
        for (var token : sessionTokens) {
            blacklist.revokeRefreshHash(token.getTokenHash(), token.getExpiresAt());
            token.setRevoked(true);
        }
    }
    private void validate(String raw) {
        if (raw == null || !raw.matches("[A-Za-z0-9_-]{43}"))
            throw new AppException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }
    private String hash(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 unavailable"); }
    }
}
