package com.zippy.auth.domain.dto.request;
import jakarta.validation.constraints.*;
public record RefreshTokenRequest(@NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String refreshToken) {
    @Override public String toString() { return "RefreshTokenRequest[redacted]"; }
}
