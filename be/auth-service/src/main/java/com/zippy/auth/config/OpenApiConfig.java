package com.zippy.auth.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.*;
@Configuration
public class OpenApiConfig {
    @Bean public org.springdoc.core.customizers.OpenApiCustomizer requestContracts() {
        return api -> {
            if (api.getComponents() == null || api.getComponents().getSchemas() == null) return;
            for (String name : java.util.List.of("LoginRequest", "RegisterRequest", "OtpRequest",
                    "ForgotPasswordRequest", "ResetPasswordRequest", "GoogleCredentialRequest", "RefreshTokenRequest")) {
                var schema = api.getComponents().getSchemas().get(name);
                if (schema != null) schema.setAdditionalProperties(false);
            }
        };
    }
    @Bean public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Zippy Auth Service").version("v1"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
