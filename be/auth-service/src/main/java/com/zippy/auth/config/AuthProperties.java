package com.zippy.auth.config;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.List;
@Validated @ConfigurationProperties(prefix = "auth")
public record AuthProperties(@NotBlank String jwtSecretBase64, @NotBlank String jwtIssuer,
    @Min(1) long accessExpirationSeconds, @Min(1) long refreshExpirationSeconds,
    List<String> corsAllowedOrigins) {}
