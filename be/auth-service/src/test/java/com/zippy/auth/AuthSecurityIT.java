package com.zippy.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zippy.auth.domain.entity.User;
import com.zippy.auth.repository.*;
import com.zippy.auth.service.*;
import com.zippy.auth.service.impl.RedisOtpService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Real HTTP + PostgreSQL + Redis/Lua. Only mail delivery and the clock are controlled. */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "auth.jwt-secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "auth.mail.from=integration@example.invalid", "auth.google.client-ids=",
    "spring.mail.host=localhost", "spring.mail.port=2525",
    "spring.mail.username=", "spring.mail.password=",
    "spring.jpa.hibernate.ddl-auto=validate", "management.health.mail.enabled=false",
    "debug=false", "logging.level.root=ERROR", "logging.level.org.springframework=ERROR"
})
class AuthSecurityIT {
    @Container static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")
        .withInitScript("db/schema/V1__auth_schema.sql");
    @Container static final GenericContainer<?> cache = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379).withCommand("sh", "-c",
            "while true; do redis-server --appendonly yes --appendfsync always --maxmemory-policy noeviction; done");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("spring.data.redis.host", cache::getHost);
        registry.add("spring.data.redis.port", () -> cache.getMappedPort(6379));
        registry.add("spring.data.redis.username", () -> "");
        registry.add("spring.data.redis.password", () -> "");
        registry.add("spring.data.redis.ssl.enabled", () -> "false");
    }
    @Autowired TestRestTemplate http;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired PasswordEncoder passwords;
    @Autowired StringRedisTemplate redis;
    @Autowired ObjectMapper mapper;
    @Autowired com.zippy.auth.security.TokenBlacklist blacklist;
    @Autowired com.zippy.auth.security.AuthRateLimiter rateLimiter;
    @MockitoBean MailService mail;
    @MockitoBean Clock clock;
    private final Map<String, String> codes = new ConcurrentHashMap<>();
    private final Instant now = Instant.parse("2026-10-04T00:00:00Z");
    private static final String PASSWORD = "Original!123";
    private static final String NEW_PASSWORD = "Changed!123";

    @BeforeEach void setup() {
        tokens.deleteAll(); users.deleteAll();
        try (var connection = redis.getConnectionFactory().getConnection()) { connection.serverCommands().flushDb(); }
        codes.clear();
        when(clock.instant()).thenReturn(now); when(clock.millis()).thenReturn(now.toEpochMilli());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        doAnswer(a -> { codes.put(a.getArgument(0), a.getArgument(1)); return null; })
            .when(mail).sendOtp(anyString(), anyString(), any());
    }
    private Map<String, String> registration(String email) {
        return Map.of("fullName", "Tên bất kỳ", "email", email, "phone", "0123456789",
            "password", PASSWORD, "confirmPassword", PASSWORD);
    }
    private ResponseEntity<JsonNode> post(String path, Object body, String flow) {
        var headers = new HttpHeaders();
        if (flow != null) headers.set("X-Auth-Flow", flow);
        return http.postForEntity("/api/auth/" + path, new HttpEntity<>(body, headers), JsonNode.class);
    }
    private String startRegister(String email) {
        var response = post("register", registration(email), null);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        String flow = response.getHeaders().getFirst("X-Auth-Flow");
        assertThat(flow).matches("[A-Za-z0-9_-]{43}");
        assertThat(response.getBody().toString()).doesNotContain(flow, PASSWORD, codes.get(email));
        return flow;
    }
    private void seed(String email) {
        var user = new User(); user.setEmail(email); user.setFullName("Test"); user.setPhone("0123456789");
        user.setPassword(passwords.encode(PASSWORD)); users.save(user);
    }
    private String startForgot(String email) {
        var response = post("forgot-password", Map.of("email", email), null);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return response.getHeaders().getFirst("X-Auth-Flow");
    }
    private JsonNode login(String email, String password) {
        var response = post("login", Map.of("email", email, "password", password), null);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return response.getBody().path("data");
    }
    private Map<String, String> resetBody() { return Map.of("newPassword", NEW_PASSWORD, "confirmNewPassword", NEW_PASSWORD); }
    private ResponseEntity<JsonNode> me(String access) {
        var headers = new HttpHeaders(); headers.setBearerAuth(access);
        return http.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
    }
    private void expireCooldowns() {
        Set<String> keys = redis.keys("auth:cooldown:*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }

    @Test void registrationUsesRealRedisAndCreatesAccountOnlyAfterOtp() {
        String email = "register@example.invalid"; String flow = startRegister(email);
        assertThat(users.count()).isZero();
        assertThat(post("register/verify-otp", Map.of("otp", codes.get(email)), null).getStatusCode().value()).isEqualTo(400);
        assertThat(post("register/verify-otp", Map.of("otp", codes.get(email)), flow).getStatusCode().value()).isEqualTo(200);
        assertThat(users.findByEmail(email).orElseThrow().getRoles()).hasSize(1);
        assertThat(post("register/verify-otp", Map.of("otp", codes.get(email)), flow).getStatusCode().value()).isEqualTo(400);
        assertThat(post("register", registration(email), null).getStatusCode().value()).isEqualTo(409);
        login(email, PASSWORD);
    }

    @Test void otpBudgetSurvivesWrongAttemptsAndResendCreatesNewBudget() {
        String email = "limited@example.invalid", flow = startRegister(email);
        String wrong = codes.get(email).equals("000000") ? "111111" : "000000";
        for (int i = 0; i < 5; i++)
            assertThat(post("register/verify-otp", Map.of("otp", wrong), flow).getStatusCode().value()).isEqualTo(400);
        assertThat(post("register/verify-otp", Map.of("otp", codes.get(email)), flow).getStatusCode().value()).isEqualTo(429);
        assertThat(users.count()).isZero();
        expireCooldowns();
        assertThat(post("register/resend-otp", Map.of(), flow).getStatusCode().value()).isEqualTo(200);
        assertThat(post("register/verify-otp", Map.of("otp", codes.get(email)), flow).getStatusCode().value()).isEqualTo(200);
    }

    @Test void concurrentRequestsCannotExceedSharedRateBudget() throws Exception {
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(12)) {
            var tasks = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 12; i++) tasks.add(pool.submit(() -> {
                start.await();
                try { rateLimiter.check("test", "same-account", 5, Duration.ofMinutes(15)); return true; }
                catch (com.zippy.auth.exception.AppException ex) {
                    assertThat(ex.getErrorCode()).isEqualTo(com.zippy.auth.exception.ErrorCode.AUTH_RATE_LIMITED);
                    return false;
                }
            }));
            start.countDown();
            int accepted = 0;
            for (var task : tasks) if (task.get(20, TimeUnit.SECONDS)) accepted++;
            assertThat(accepted).isEqualTo(5);
        }
        var keys = redis.keys("auth:limit:test:*");
        assertThat(keys).hasSize(1);
        assertThat(redis.getExpire(keys.iterator().next())).isBetween(1L, 900L);
    }

    @Test void resetRequiresOwnVerifiedFlowAndRevokesOldSessions() {
        seed("victim@example.invalid"); seed("other@example.invalid");
        JsonNode oldTokens = login("victim@example.invalid", PASSWORD);
        String flow = startForgot("victim@example.invalid"), other = startForgot("other@example.invalid");
        assertThat(post("reset-password", resetBody(), flow).getStatusCode().value()).isEqualTo(400);
        assertThat(post("forgot-password/verify-otp", Map.of("otp", codes.get("victim@example.invalid")), flow)
            .getStatusCode().value()).isEqualTo(200);
        assertThat(post("reset-password", resetBody(), null).getStatusCode().value()).isEqualTo(400);
        assertThat(post("reset-password", resetBody(), other).getStatusCode().value()).isEqualTo(400);
        assertThat(post("reset-password", resetBody(), flow).getStatusCode().value()).isEqualTo(200);
        assertThat(post("reset-password", resetBody(), flow).getStatusCode().value()).isEqualTo(400);
        assertThat(post("login", Map.of("email", "victim@example.invalid", "password", PASSWORD), null).getStatusCode().value()).isEqualTo(401);
        login("victim@example.invalid", NEW_PASSWORD);
        login("other@example.invalid", PASSWORD);
        assertThat(me(oldTokens.path("accessToken").asText()).getStatusCode().value()).isEqualTo(401);
        assertThat(post("refresh", Map.of("refreshToken", oldTokens.path("refreshToken").asText()), null).getStatusCode().value()).isEqualTo(401);
    }

    @Test void logoutBlocksSameAccessTokenAndBlacklistSurvivesRedisRestart() throws Exception {
        seed("logout@example.invalid"); var data = login("logout@example.invalid", PASSWORD);
        String access = data.path("accessToken").asText();
        var headers = new HttpHeaders(); headers.setBearerAuth(access);
        assertThat(me(access).getStatusCode().value()).isEqualTo(200);
        assertThat(http.postForEntity("/api/auth/logout", new HttpEntity<>(headers), JsonNode.class)
            .getStatusCode().value()).isEqualTo(200);
        assertThat(me(access).getStatusCode().value()).isEqualTo(401);
        Set<String> revoked = redis.keys("auth:revoked:*");
        assertThat(revoked).hasSize(2);
        String accessKey = revoked.stream().filter(k -> !k.startsWith("auth:revoked:refresh:")).findFirst().orElseThrow();
        long ttl = redis.getExpire(accessKey);
        assertThat(ttl).isBetween(895L, 900L);
        String refreshKey = revoked.stream().filter(k -> k.startsWith("auth:revoked:refresh:")).findFirst().orElseThrow();
        assertThat(redis.getExpire(refreshKey)).isBetween(604790L, 604800L);
        assertThat(revoked).allSatisfy(k -> assertThat(k).doesNotContain(access, data.path("refreshToken").asText()));
        // Restart Redis itself: Docker reassigns random published ports on container restart.
        cache.execInContainer("redis-cli", "shutdown");
        // Wait for Redis using its bounded container command, not a fixed sleep.
        boolean ready = false;
        for (int i = 0; i < 20; i++) {
            try { if (cache.execInContainer("redis-cli", "ping").getStdout().contains("PONG")) { ready = true; break; } }
            catch (Exception ignored) { }
            Thread.sleep(100);
        }
        assertThat(ready).isTrue();
        ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) redis.getConnectionFactory()).resetConnection();
        assertThat(me(access).getStatusCode().value()).isEqualTo(401);
        assertThat(post("refresh", Map.of("refreshToken", data.path("refreshToken").asText()), null).getStatusCode().value()).isEqualTo(401);
    }

    @Test void logoutFindsRotatedRefreshAndLeavesOtherDeviceSignedIn() {
        seed("sessions@example.invalid");
        var first = login("sessions@example.invalid", PASSWORD);
        var otherDevice = login("sessions@example.invalid", PASSWORD);
        var rotated = post("refresh", Map.of("refreshToken", first.path("refreshToken").asText()), null);
        assertThat(rotated.getStatusCode().value()).isEqualTo(200);
        var current = rotated.getBody().path("data");
        var headers = new HttpHeaders(); headers.setBearerAuth(first.path("accessToken").asText());
        assertThat(http.postForEntity("/api/auth/logout", new HttpEntity<>(headers), JsonNode.class)
            .getStatusCode().value()).isEqualTo(200);
        assertThat(me(current.path("accessToken").asText()).getStatusCode().value()).isEqualTo(401);
        assertThat(post("refresh", Map.of("refreshToken", current.path("refreshToken").asText()), null)
            .getStatusCode().value()).isEqualTo(401);
        assertThat(me(otherDevice.path("accessToken").asText()).getStatusCode().value()).isEqualTo(200);
        assertThat(post("refresh", Map.of("refreshToken", otherDevice.path("refreshToken").asText()), null)
            .getStatusCode().value()).isEqualTo(200);
        assertThat(redis.keys("auth:revoked:refresh:*")).hasSize(2);
    }

    @Test void redisAutomaticallyDeletesExpiredBlacklistEntries() {
        UUID session = UUID.randomUUID();
        blacklist.revoke("test-access", now.plusSeconds(1));
        blacklist.revokeRefreshHash("test-refresh-hash", now.plusSeconds(1));
        blacklist.revokeSession(session, now.plusSeconds(1));
        assertThat(blacklist.isRevoked("test-access")).isTrue();
        assertThat(blacklist.isRefreshRevoked("test-refresh-hash")).isTrue();
        assertThat(blacklist.isSessionRevoked(session)).isTrue();
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(blacklist.isRevoked("test-access")).isFalse();
            assertThat(blacklist.isRefreshRevoked("test-refresh-hash")).isFalse();
            assertThat(blacklist.isSessionRevoked(session)).isFalse();
        });
        blacklist.revoke("already-expired", now.minusSeconds(1));
        assertThat(blacklist.isRevoked("already-expired")).isFalse();
    }

    @Test void refreshRacingLogoutCannotLeaveUsableTokens() throws Exception {
        seed("logout-race@example.invalid");
        var data = login("logout-race@example.invalid", PASSWORD);
        var headers = new HttpHeaders(); headers.setBearerAuth(data.path("accessToken").asText());
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var refreshing = pool.submit(() -> {
                start.await();
                return post("refresh", Map.of("refreshToken", data.path("refreshToken").asText()), null);
            });
            var loggingOut = pool.submit(() -> {
                start.await();
                return http.postForEntity("/api/auth/logout", new HttpEntity<>(headers), JsonNode.class);
            });
            start.countDown();
            assertThat(loggingOut.get(20, TimeUnit.SECONDS).getStatusCode().value()).isEqualTo(200);
            var refreshed = refreshing.get(20, TimeUnit.SECONDS);
            assertThat(refreshed.getStatusCode().value()).isIn(200, 401);
            if (refreshed.getStatusCode().value() == 200) {
                var issued = refreshed.getBody().path("data");
                assertThat(me(issued.path("accessToken").asText()).getStatusCode().value()).isEqualTo(401);
                assertThat(post("refresh", Map.of("refreshToken", issued.path("refreshToken").asText()), null)
                    .getStatusCode().value()).isEqualTo(401);
            }
        }
    }

    @Test void initialRequestsAndResendsShareSixtySecondCooldown() {
        String flow = startRegister("cooldown@example.invalid");
        assertThat(post("register", registration("cooldown@example.invalid"), null).getStatusCode().value()).isEqualTo(429);
        assertThat(post("register/resend-otp", Map.of(), flow).getStatusCode().value()).isEqualTo(429);
        seed("forgot@example.invalid"); String resetFlow = startForgot("forgot@example.invalid");
        assertThat(post("forgot-password", Map.of("email", "forgot@example.invalid"), null).getStatusCode().value()).isEqualTo(429);
        assertThat(post("forgot-password/resend-otp", Map.of(), resetFlow).getStatusCode().value()).isEqualTo(429);
        for (String key : redis.keys("auth:cooldown:*")) assertThat(redis.getExpire(key)).isBetween(55L, 60L);
    }

    @Test void wrongExpiredOtpAndResendWorkWithoutLosingPendingData() {
        String email = "expiry@example.invalid", flow = startRegister(email);
        String old = codes.get(email), wrong = old.equals("000000") ? "000001" : "000000";
        assertThat(post("register/verify-otp", Map.of("otp", wrong), flow).getBody().path("errorCode").asText()).isEqualTo("AUTH_INVALID_OTP");
        when(clock.millis()).thenReturn(now.plusSeconds(180).toEpochMilli());
        assertThat(post("register/verify-otp", Map.of("otp", old), flow).getBody().path("errorCode").asText()).isEqualTo("AUTH_OTP_EXPIRED");
        expireCooldowns();
        assertThat(post("register/resend-otp", Map.of(), flow).getStatusCode().value()).isEqualTo(200);
        assertThat(codes.get(email)).isNotEqualTo(old);
        assertThat(post("register/verify-otp", Map.of("otp", old), flow).getBody().path("errorCode").asText()).isEqualTo("AUTH_INVALID_OTP");
        assertThat(post("register/verify-otp", Map.of("otp", codes.get(email)), flow).getStatusCode().value()).isEqualTo(200);
    }

    @Test void resendUsesItsOwnUserAndPurpose() {
        String a = startRegister("a@example.invalid"), b = startRegister("b@example.invalid");
        expireCooldowns(); clearInvocations(mail);
        assertThat(post("register/resend-otp", Map.of(), a).getStatusCode().value()).isEqualTo(200);
        verify(mail).sendOtp(eq("a@example.invalid"), anyString(), eq(OtpService.Purpose.REGISTER));
        verify(mail, never()).sendOtp(eq("b@example.invalid"), anyString(), any());
        assertThat(post("forgot-password/verify-otp", Map.of("otp", codes.get("b@example.invalid")), b).getStatusCode().value()).isEqualTo(400);
    }

    @Test void concurrentOtpVerificationAndResetAllowOneWinner() throws Exception {
        seed("race@example.invalid"); String flow = startForgot("race@example.invalid");
        assertThat(race(() -> post("forgot-password/verify-otp", Map.of("otp", codes.get("race@example.invalid")), flow).getStatusCode().value()))
            .containsExactlyInAnyOrder(200, 400);
        assertThat(race(() -> post("reset-password", resetBody(), flow).getStatusCode().value())).containsExactlyInAnyOrder(200, 400);
    }

    @Test void concurrentResendAllowsOneMessage() throws Exception {
        String flow = startRegister("resend@example.invalid"); expireCooldowns(); clearInvocations(mail);
        assertThat(race(() -> post("register/resend-otp", Map.of(), flow).getStatusCode().value())).containsExactlyInAnyOrder(200, 429);
        verify(mail, times(1)).sendOtp(eq("resend@example.invalid"), anyString(), any());
    }

    @Test void failedMailRestoresStateAndAllowsRetry() {
        doThrow(new com.zippy.auth.exception.AppException(com.zippy.auth.exception.ErrorCode.AUTH_MAIL_UNAVAILABLE))
            .when(mail).sendOtp(eq("mail@example.invalid"), anyString(), any());
        assertThat(post("register", registration("mail@example.invalid"), null).getStatusCode().value()).isEqualTo(503);
        assertThat(redis.keys("auth:flow:*")).isEmpty();
        assertThat(redis.keys("auth:cooldown:*")).isEmpty();
        doAnswer(a -> { codes.put(a.getArgument(0), a.getArgument(1)); return null; })
            .when(mail).sendOtp(eq("mail@example.invalid"), anyString(), any());
        startRegister("mail@example.invalid");
    }

    @Test void longPasswordAndFullNameRoundTripWithPostgres() {
        var body = new HashMap<>(registration("long@example.invalid"));
        body.put("fullName", "Tên".repeat(100));
        body.put("password", "Đ".repeat(80) + "!"); body.put("confirmPassword", body.get("password"));
        var response = post("register", body, null);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        String flow = response.getHeaders().getFirst("X-Auth-Flow");
        assertThat(post("register/verify-otp", Map.of("otp", codes.get("long@example.invalid")), flow).getStatusCode().value()).isEqualTo(200);
        login("long@example.invalid", body.get("password"));
        assertThat(users.findByEmail("long@example.invalid").orElseThrow().getFullName()).isEqualTo(body.get("fullName"));
    }

    @Test void rollbackRestoresClaimInsteadOfLosingOtp() {
        String flow = startRegister("rollback@example.invalid"), code = codes.get("rollback@example.invalid");
        seed("rollback@example.invalid");
        assertThat(post("register/verify-otp", Map.of("otp", code), flow).getStatusCode().value()).isEqualTo(409);
        users.deleteAll();
        assertThat(post("register/verify-otp", Map.of("otp", code), flow).getStatusCode().value()).isEqualTo(200);
    }

    private List<Integer> race(Callable<Integer> request) throws Exception {
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Integer> action = () -> { start.await(); return request.call(); };
            var a = pool.submit(action); var b = pool.submit(action); start.countDown();
            return List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
        }
    }
}
