package com.zippy.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.zippy.auth.domain.entity.User;
import com.zippy.auth.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:authentication;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "auth.jwt-secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "debug=false", "logging.level.root=WARN", "logging.level.org.springframework=WARN"
})
@Import(AuthenticationTest.Probes.class)
@org.springframework.test.context.ActiveProfiles("test")
class AuthenticationTest {
    @org.springframework.test.context.bean.override.mockito.MockitoBean com.zippy.auth.security.AuthRateLimiter rateLimiter;
    @org.springframework.test.context.bean.override.mockito.MockitoBean com.zippy.auth.service.OtpService otp;
    @org.springframework.test.context.bean.override.mockito.MockitoBean com.zippy.auth.security.TokenBlacklist blacklist;
    private final Set<String> revokedAccess = new HashSet<>();
    @Autowired TestRestTemplate http;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired PasswordEncoder passwords;
    private static final String PASSWORD = "Correct!123";

    @BeforeEach void seed() {
        revokedAccess.clear();
        org.mockito.Mockito.when(blacklist.isRevoked(org.mockito.ArgumentMatchers.anyString()))
            .thenAnswer(a -> revokedAccess.contains(a.getArgument(0)));
        org.mockito.Mockito.doAnswer(a -> { revokedAccess.add(a.getArgument(0)); return null; })
            .when(blacklist).revoke(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
        tokens.deleteAll(); users.deleteAll();
        var user = new User();
        user.setEmail("test@gmail.com"); user.setPhone("0123456789"); user.setFullName("Test");
        user.setPassword(passwords.encode(PASSWORD)); users.save(user);
    }
    private ResponseEntity<JsonNode> post(String path, Object body) {
        return http.postForEntity("/api/auth/" + path, body, JsonNode.class);
    }
    private JsonNode login() {
        var response = post("login", Map.of("email", "test@gmail.com", "password", PASSWORD));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().toString()).doesNotContain(PASSWORD, "password", "$2");
        assertThat(response.getHeaders().getFirst("Set-Cookie")).isNull();
        return response.getBody().path("data");
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "012345678", "01234567890", "012345678901", "012345678a"
    })
    void invalidRegistrationPhoneNeverStartsOtp(String phone) {
        var response = post("register", Map.of("fullName", "Test", "email", "new@example.com",
            "phone", phone, "password", PASSWORD, "confirmPassword", PASSWORD));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText())
            .isEqualTo(com.zippy.auth.constant.AuthMessages.PHONE);
        org.mockito.Mockito.verifyNoInteractions(otp);
        assertThat(users.existsByEmail("new@example.com")).isFalse();
    }
    @Test void twelveDigitRegistrationPasswordNeverStartsOtp() {
        var response = post("register", Map.of("fullName", "Test", "email", "new@example.com",
            "phone", "0123456789", "password", "123456789012", "confirmPassword", "123456789012"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText())
            .isEqualTo(com.zippy.auth.constant.AuthMessages.PASSWORD);
        org.mockito.Mockito.verifyNoInteractions(otp);
    }
    @ParameterizedTest @CsvSource(value = {
        "'',Correct!123,Vui lòng nhập đầy đủ thông tin",
        "test@gmail.com,'',Vui lòng nhập đầy đủ thông tin",
        "'','',Vui lòng nhập đầy đủ thông tin",
        "invalid,Correct!123,Email không đúng định dạng"
    })
    void validation(String email, String password, String message) {
        var response = post("login", Map.of("email", email, "password", password));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText()).isEqualTo(message);
    }
    @Test void missingFieldsAndUnknownFields() {
        for (var body : List.of(Map.of(), Map.of("email", "test@gmail.com"), Map.of("password", PASSWORD))) {
            var response = post("login", body);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().path("message").asText()).isEqualTo("Vui lòng nhập đầy đủ thông tin");
        }
        for (String field : List.of("username", "phone", "rememberMe", "role", "userId", "status", "provider", "providerId")) {
            assertThat(post("login", Map.of("email", "test@gmail.com", "password", PASSWORD, field, "extra"))
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
    @Test void missingEmailAndWrongPasswordAreIndistinguishable() {
        var missing = post("login", Map.of("email", "missing@gmail.com", "password", PASSWORD));
        var wrong = post("login", Map.of("email", "test@gmail.com", "password", "wrong"));
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(wrong.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(missing.getBody()).isEqualTo(wrong.getBody());
        assertThat(wrong.getBody().path("message").asText()).isEqualTo("Email hoặc mật khẩu không chính xác");
        assertThat(tokens.count()).isZero();
    }
    @Test void bcryptLoginJwtAndTokenLifecycle() {
        assertThat(passwords).isInstanceOf(com.zippy.auth.security.CompatiblePasswordEncoder.class);
        assertThat(users.findByEmail("test@gmail.com").orElseThrow().getPassword()).startsWith("{pbkdf2}");
        JsonNode data = login();
        var headers = new HttpHeaders(); headers.setBearerAuth(data.path("accessToken").asText());
        var me = http.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().path("data").path("email").asText()).isEqualTo("test@gmail.com");
        String raw = data.path("refreshToken").asText();
        assertThat(tokens.findAll()).allSatisfy(t -> assertThat(t.getTokenHash()).hasSize(64).isNotEqualTo(raw));
        var refreshed = post("refresh", Map.of("refreshToken", raw));
        assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(post("refresh", Map.of("refreshToken", raw)).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        String next = refreshed.getBody().path("data").path("refreshToken").asText();
        assertThat(next).isNotEqualTo(raw);
        var logoutHeaders = new HttpHeaders();
        logoutHeaders.setBearerAuth(refreshed.getBody().path("data").path("accessToken").asText());
        assertThat(post("logout", new HttpEntity<>(Map.of("refreshToken", next), logoutHeaders)).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(http.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(logoutHeaders), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(post("logout", new HttpEntity<>(Map.of("refreshToken", next), logoutHeaders)).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(post("refresh", Map.of("refreshToken", next)).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    @Test void expiredAndUnknownRefreshTokensFail() {
        String raw = login().path("refreshToken").asText();
        var token = tokens.findAll().getFirst(); token.setExpiresAt(Instant.now().minusSeconds(1)); tokens.save(token);
        assertThat(post("refresh", Map.of("refreshToken", raw)).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(post("refresh", Map.of("refreshToken", "a".repeat(43))).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    @Test void oversizedPasswordCannotAuthenticateByTruncation() {
        var response = post("login", Map.of("email", "test@gmail.com", "password", "a".repeat(73)));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().path("message").asText()).isEqualTo("Email hoặc mật khẩu không chính xác");
    }
    @Test void concurrentRefreshAllowsOnlyOneRotation() throws Exception {
        String raw = login().path("refreshToken").asText();
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Integer> refresh = () -> {
                start.await();
                return post("refresh", Map.of("refreshToken", raw)).getStatusCode().value();
            };
            var first = executor.submit(refresh); var second = executor.submit(refresh);
            start.countDown();
            assertThat(List.of(first.get(15, java.util.concurrent.TimeUnit.SECONDS),
                second.get(15, java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(200, 401);
        }
    }
    @Test void securityErrorsAreJson() {
        var unauthenticated = http.getForEntity("/api/auth/me", JsonNode.class);
        assertThat(unauthenticated.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        var headers = new HttpHeaders(); headers.setBearerAuth(login().path("accessToken").asText());
        var denied = http.exchange("/test/denied", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(denied.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(denied.getBody().path("errorCode").asText()).isEqualTo("AUTH_FORBIDDEN");
        headers.setBearerAuth("invalid");
        var invalid = http.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        for (var response : List.of(unauthenticated, denied, invalid)) {
            assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
            assertThat(response.getBody().toString()).doesNotContain("trace", "Exception", "password");
        }
    }
    @Test void unconfiguredGoogleFailsClosed() {
        assertThat(post("google", Map.of("credential", "invalid")).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
    @Test void blacklistOutageFailsClosedWith503() {
        String access = login().path("accessToken").asText();
        org.mockito.Mockito.when(blacklist.isRevoked(org.mockito.ArgumentMatchers.anyString()))
            .thenThrow(new org.springframework.data.redis.RedisConnectionFailureException("simulated outage"));
        var headers = new HttpHeaders(); headers.setBearerAuth(access);
        var response = http.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().path("errorCode").asText()).isEqualTo("AUTH_DEPENDENCY_UNAVAILABLE");
    }
    @Test void unsupportedRequestsKeepJsonEnvelopeAndHttpStatus() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        var media = http.postForEntity("/api/auth/login", new HttpEntity<>("sensitive-request", headers), JsonNode.class);
        assertThat(media.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        headers.setBearerAuth(login().path("accessToken").asText());
        var method = http.exchange("/api/auth/login", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(method.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(method.getHeaders().getAllow()).contains(HttpMethod.POST);
        for (var response : List.of(media, method)) {
            assertThat(response.getBody().path("errorCode").asText()).isEqualTo("VALIDATION_ERROR");
            assertThat(response.getBody().path("message").asText()).isEqualTo("Unsupported request");
            assertThat(response.getBody().properties()).extracting(Map.Entry::getKey)
                .containsExactlyInAnyOrder("success", "message", "errorCode");
        }
    }

    @Test void malformedJsonAndUnexpectedFailuresNeverExposeDetails() {
        var headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        var malformed = post("login", new HttpEntity<>("{\"password\":\"private-secret\",", headers));
        assertThat(malformed.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(malformed.getBody().path("errorCode").asText()).isEqualTo("VALIDATION_ERROR");
        headers.setBearerAuth(login().path("accessToken").asText());
        var failure = http.exchange("/test/failure", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(failure.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(failure.getBody().path("errorCode").asText()).isEqualTo("INTERNAL_ERROR");
        for (var response : List.of(malformed, failure))
            assertThat(response.getBody().toString()).doesNotContain("private-secret", "trace", "Exception", "password");
    }
    @TestConfiguration static class Probes {
        @Bean ProbeController probeController() { return new ProbeController(); }
    }
    @RestController static class ProbeController {
        @GetMapping("/test/failure")
        public String failure() { throw new IllegalStateException("private-secret"); }
        @GetMapping("/test/denied") @PreAuthorize("denyAll()")
        public String denied() { return "unreachable"; }
    }
}
