package com.zippy.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.zippy.auth.domain.entity.User;
import com.zippy.auth.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:google;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "auth.jwt-secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "auth.google.client-ids=test-client", "debug=false", "logging.level.root=WARN", "logging.level.org.springframework=WARN"
})
@org.springframework.test.context.ActiveProfiles("test")
class GoogleLoginTest {
    @MockitoBean com.zippy.auth.service.OtpService otp;
    @MockitoBean com.zippy.auth.security.TokenBlacklist blacklist;
    @Autowired TestRestTemplate http;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @MockitoBean(name = "googleJwtDecoder") JwtDecoder decoder;

    @BeforeEach void seed() {
        tokens.deleteAll(); users.deleteAll();
        var user = new User(); user.setEmail("test@gmail.com"); user.setFullName("Test");
        user.setPhone("0123456789");
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("Local!123"));
        users.save(user);
    }
    private Jwt identity(String email, boolean verified) {
        return Jwt.withTokenValue("valid").header("alg", "RS256").subject("google-subject")
            .issuer("https://accounts.google.com").audience(List.of("test-client"))
            .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300))
            .claim("email", email).claim("email_verified", verified).build();
    }
    @Test void existingVerifiedGoogleAccountGetsInternalTokens() {
        when(decoder.decode("valid")).thenReturn(identity("test@gmail.com", true));
        var response = http.postForEntity("/api/auth/google", Map.of("credential", "valid"), JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().path("data").path("accessToken").asText()).isNotBlank();
        assertThat(response.getBody().toString()).doesNotContain("password", "google-subject");
        assertThat(users.count()).isEqualTo(1);
        assertThat(tokens.count()).isEqualTo(1);
    }
    @Test void rejectsInvalidUnverifiedAndNonAuthoritativeIdentities() {
        when(decoder.decode("invalid")).thenThrow(new BadJwtException("private decoder details"));
        when(decoder.decode("unverified")).thenReturn(identity("test@gmail.com", false));
        when(decoder.decode("third-party")).thenReturn(identity("test@example.com", true));
        for (String credential : List.of("invalid", "unverified", "third-party")) {
            var response = http.postForEntity("/api/auth/google", Map.of("credential", credential), JsonNode.class);
            assertThat(response.getStatusCode().value()).isEqualTo(401);
            assertThat(response.getBody().toString()).doesNotContain("Exception", "trace", "private decoder details");
        }
        assertThat(tokens.count()).isZero();
    }
    @Test void newAccountDoesNotInventPhonePasswordOrModifySchema() {
        when(decoder.decode("new")).thenReturn(identity("new@gmail.com", true));
        var response = http.postForEntity("/api/auth/google", Map.of("credential", "new"), JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(users.count()).isEqualTo(1);
        assertThat(tokens.count()).isZero();
    }
}
