package com.zippy.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.*;
import java.util.*;

@Configuration @EnableConfigurationProperties(GoogleProperties.class)
public class GoogleConfig {
    @Bean("googleJwtDecoder")
    public JwtDecoder googleJwtDecoder(GoogleProperties properties, Clock clock) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        var decoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .jwsAlgorithm(SignatureAlgorithm.RS256).restOperations(new RestTemplate(factory)).build();
        decoder.setJwtValidator(validator(properties, clock));
        return decoder;
    }

    public static org.springframework.security.oauth2.core.OAuth2TokenValidator<Jwt> validator(
            GoogleProperties properties, Clock clock) {
        var timestamps = new JwtTimestampValidator(Duration.ofSeconds(30));
        timestamps.setClock(clock);
        return new DelegatingOAuth2TokenValidator<>(timestamps,
            new JwtClaimValidator<String>("iss", value -> "https://accounts.google.com".equals(value)
                || "accounts.google.com".equals(value)),
            new JwtClaimValidator<List<String>>("aud", values -> values != null && properties.clientIds() != null
                && values.stream().anyMatch(properties.clientIds()::contains)),
            new JwtClaimValidator<String>("sub", value -> value != null && !value.isBlank()),
            new JwtClaimValidator<Instant>("exp", Objects::nonNull));
    }
}
