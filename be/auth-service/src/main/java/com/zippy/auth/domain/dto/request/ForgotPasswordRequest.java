package com.zippy.auth.domain.dto.request;
import jakarta.validation.constraints.*;
import static com.zippy.auth.constant.AuthMessages.*;
public record ForgotPasswordRequest(
    @io.swagger.v3.oas.annotations.media.Schema(description = "Email / Gmail")
    @NotBlank(message = REQUIRED) @Email(message = EMAIL) @Size(max = 254, message = EMAIL) String email) {}
