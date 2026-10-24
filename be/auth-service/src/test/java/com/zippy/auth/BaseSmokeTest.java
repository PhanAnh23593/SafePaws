package com.zippy.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.zippy.auth.repository.UserRepository;
import com.zippy.auth.repository.RoleRepository;
import com.zippy.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Stream;
import static com.zippy.auth.constant.AuthMessages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:base-smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "auth.jwt-secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "debug=false", "logging.level.root=WARN", "logging.level.org.springframework=WARN"
})
@org.springframework.test.context.ActiveProfiles("test")
class BaseSmokeTest {
    @Autowired TestRestTemplate http;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired RefreshTokenRepository tokens;

    static Map<String, Map<String, String>> screenRequests() {
        return Map.of(
            "login", Map.of("email", "a@example.com", "password", "ABCDE!"),
            "register", Map.of("fullName", "Tên", "email", "a@example.com", "phone", "0123456789",
                "password", "ABCDE!", "confirmPassword", "ABCDE!"),
            "register/verify-otp", Map.of("otp", "123456"),
            "forgot-password", Map.of("email", "a@example.com"),
            "forgot-password/verify-otp", Map.of("otp", "123456"),
            "reset-password", Map.of("newPassword", "ABCDE!", "confirmNewPassword", "ABCDE!"));
    }

    static Stream<Arguments> missingFields() {
        return screenRequests().entrySet().stream().flatMap(screen -> screen.getValue().keySet().stream()
            .flatMap(field -> Stream.of("missing", "null", "blank").map(kind -> {
                var body = new HashMap<>(screen.getValue());
                if (kind.equals("missing")) body.remove(field);
                else body.put(field, kind.equals("null") ? null : " ");
                return Arguments.of(screen.getKey(), field, kind, body);
            })));
    }

    @ParameterizedTest(name = "{0}: {1} {2}") @MethodSource("missingFields")
    void allScreensShareRequiredMessage(String path, String field, String kind, Map<String, String> body) {
        assertValidation(path, body, REQUIRED);
    }

    @Test void allScreensRejectAdditionalProperties() {
        screenRequests().forEach((path, request) -> {
            for (String field : List.of("userId", "role", "provider", "resetToken", "unexpected")) {
                var body = new HashMap<>(request);
                body.put(field, "private-value");
                assertValidation(path, body, "Invalid request");
            }
        });
    }

    @Test void allScreensShareValidationMessagesAndPriority() {
        for (String path : List.of("login", "register", "forgot-password")) {
            var body = new HashMap<>(screenRequests().get(path));
            body.put("email", "invalid");
            assertValidation(path, body, EMAIL);
        }
        var register = new HashMap<>(screenRequests().get("register"));
        register.put("phone", "123");
        assertValidation("register", register, PHONE);
        register.put("email", "invalid");
        assertValidation("register", register, EMAIL);
        register.put("fullName", "");
        assertValidation("register", register, REQUIRED);
        for (String path : List.of("register", "reset-password")) {
            var body = new HashMap<>(screenRequests().get(path));
            String password = path.equals("register") ? "password" : "newPassword";
            String confirm = path.equals("register") ? "confirmPassword" : "confirmNewPassword";
            for (String invalid : List.of("Ab!", "abcdef!", "ABCDEF")) {
                body.put(password, invalid); body.put(confirm, invalid);
                assertValidation(path, body, PASSWORD);
            }
            body.put(password, "ABCDE!"); body.put(confirm, "OTHER!");
            assertValidation(path, body, MISMATCH);
        }
        for (String path : List.of("register/verify-otp", "forgot-password/verify-otp")) {
            assertValidation(path, Map.of("otp", "12345"), REQUIRED);
            for (String invalid : List.of("1234567", "abcdef", "１２３４５６"))
                assertValidation(path, Map.of("otp", invalid), OTP);
        }
        assertThat(users.count()).isZero();
        assertThat(tokens.count()).isZero();
    }

    private void assertValidation(String path, Object body, String message) {
        var response = http.postForEntity("/api/auth/" + path, body, JsonNode.class);
        assertThat(response.getStatusCode()).as(path).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody().properties()).extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("success", "message", "errorCode");
        assertThat(response.getBody().path("success").asBoolean()).isFalse();
        assertThat(response.getBody().path("errorCode").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().path("message").asText()).isEqualTo(message);
    }

    @Test void deploymentProbesArePublicAndHideDetails() {
        for (String group : List.of("liveness", "readiness")) {
            var response = http.getForEntity("/actuator/health/" + group, JsonNode.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().properties()).extracting(Map.Entry::getKey).containsExactly("status");
            assertThat(response.getBody().path("status").asText()).isEqualTo("UP");
        }
        assertThat(http.getForEntity("/actuator/env", JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void infrastructureStartsWithoutSeedingBusinessData() {
        var health = http.getForEntity("/actuator/health", JsonNode.class);
        assertThat(health.getStatusCode().value()).isEqualTo(200);
        assertThat(health.getBody().path("status").asText()).isEqualTo("UP");
        assertThat(http.getForEntity("/swagger-ui/index.html", String.class)
            .getStatusCode().value()).isEqualTo(200);
        assertThat(users.count()).isZero();
        assertThat(roles.count()).isZero();
        assertThat(tokens.count()).isZero();
    }

    @Test
    void recoveryEndpointsRequireTheirOwnFlow() {
        Map<String, Object> bodies = Map.ofEntries(
            Map.entry("register/verify-otp", Map.of("otp", "123456")),
            Map.entry("register/resend-otp", Map.of()),
            Map.entry("forgot-password/verify-otp", Map.of("otp", "123456")),
            Map.entry("forgot-password/resend-otp", Map.of()),
            Map.entry("reset-password", Map.of("newPassword", "ABCDE!", "confirmNewPassword", "ABCDE!")));
        bodies.forEach((path, body) -> {
            var response = http.postForEntity("/api/auth/" + path, body, JsonNode.class);
            assertThat(response.getStatusCode().value()).as(path).isEqualTo(400);
            assertThat(response.getBody().path("errorCode").asText()).isEqualTo("AUTH_FLOW_INVALID");
        });
        assertThat(users.count()).isZero();
        assertThat(tokens.count()).isZero();
    }

    @Test
    void swaggerKeepsTheAgreedRequestFields() {
        var response = http.getForEntity("/v3/api-docs", JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        var schemas = response.getBody().path("components").path("schemas");
        Map<String, String[]> fields = Map.of(
            "LoginRequest", new String[]{"email", "password"},
            "RegisterRequest", new String[]{"fullName", "email", "phone", "password", "confirmPassword"},
            "OtpRequest", new String[]{"otp"},
            "ForgotPasswordRequest", new String[]{"email"},
            "ResetPasswordRequest", new String[]{"newPassword", "confirmNewPassword"});
        fields.forEach((name, expected) -> assertThat(schemas.path(name).path("properties").properties())
            .extracting(Map.Entry::getKey).containsExactlyInAnyOrder(expected));
        fields.forEach((name, expected) -> {
            assertThat(schemas.path(name).path("additionalProperties").isBoolean()).isTrue();
            assertThat(schemas.path(name).path("additionalProperties").asBoolean()).isFalse();
        });
        Map.of("login", "LoginRequest", "register", "RegisterRequest", "forgot-password", "ForgotPasswordRequest",
            "reset-password", "ResetPasswordRequest").forEach((path, schema) ->
                assertThat(response.getBody().path("paths").path("/api/auth/" + path).path("post")
                    .path("requestBody").path("content").path("application/json").path("schema").path("$ref").asText())
                    .endsWith("/" + schema));
        assertThat(schemas.path("LoginRequest").path("properties").path("email").path("description").asText())
            .isEqualTo("Email / Gmail");
        assertThat(schemas.path("RegisterRequest").path("properties").path("fullName").path("description").asText())
            .isEqualTo("Họ và tên / Tên đăng nhập");
        for (String path : java.util.List.of("/api/auth/register/verify-otp", "/api/auth/forgot-password/verify-otp")) {
            assertThat(response.getBody().path("paths").path(path).path("post").path("requestBody")
                .path("content").path("application/json").path("schema").path("$ref").asText())
                .endsWith("/OtpRequest");
        }
    }

    @Test
    void jwtFilterStillRejectsInvalidCredentials() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");
        var response = http.exchange("/api/auth/me", HttpMethod.GET,
            new HttpEntity<>(headers), JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().path("errorCode").asText()).isEqualTo("AUTH_UNAUTHORIZED");
    }
}
