package com.zippy.auth.service;
import com.zippy.auth.domain.entity.User;
public interface TokenService {
    IssuedRefreshToken issue(User user);
    IssuedRefreshToken rotate(String rawToken);
    void revoke(String rawToken, java.util.UUID userId);
    void revokeAll(java.util.UUID userId);
    void revokeSession(java.util.UUID userId, java.util.UUID sessionId);
    record IssuedRefreshToken(String rawToken, User user, java.util.UUID sessionId) {
        @Override public String toString() { return "IssuedRefreshToken[redacted]"; }
    }
}
