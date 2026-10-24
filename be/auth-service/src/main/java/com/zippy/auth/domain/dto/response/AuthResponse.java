package com.zippy.auth.domain.dto.response;
public record AuthResponse(String accessToken, String refreshToken, String tokenType,
                           long expiresIn, UserResponse user) {
    @Override public String toString() { return "AuthResponse[redacted]"; }
}
