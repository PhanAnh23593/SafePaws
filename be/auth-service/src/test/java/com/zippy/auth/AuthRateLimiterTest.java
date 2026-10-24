package com.zippy.auth;

import com.zippy.auth.exception.*;
import com.zippy.auth.security.AuthRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import java.time.Duration;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthRateLimiterTest {
    @Test void rejectsExhaustedBudgetAndFailsClosedOnMissingResult() {
        var redis = mock(StringRedisTemplate.class);
        var limiter = new AuthRateLimiter(redis);
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(0L, null, 1L);
        assertThatThrownBy(() -> limiter.check("login", "user@example.com", 10, Duration.ofMinutes(15)))
            .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AUTH_RATE_LIMITED));
        assertThatThrownBy(() -> limiter.check("login", "user@example.com", 10, Duration.ofMinutes(15)))
            .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AUTH_DEPENDENCY_UNAVAILABLE));
        assertThatCode(() -> limiter.check("login", "user@example.com", 10, Duration.ofMinutes(15))).doesNotThrowAnyException();
    }
}
