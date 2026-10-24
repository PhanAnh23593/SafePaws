package com.zippy.auth.domain.dto.request;
import jakarta.validation.constraints.*;
import static com.zippy.auth.constant.AuthMessages.*;
public record LoginRequest(
    @io.swagger.v3.oas.annotations.media.Schema(description = "Email / Gmail")
    @NotBlank(message = REQUIRED) @Email(message = EMAIL) @Size(max = 254, message = EMAIL) String email,
    @io.swagger.v3.oas.annotations.media.Schema(description = "Mật khẩu", format = "password", accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = REQUIRED) String password) {
    @Override public String toString() { return "LoginRequest[redacted]"; }
}
