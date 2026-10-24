package com.zippy.auth;

import com.zippy.auth.config.AuthProperties;
import com.zippy.auth.domain.dto.request.LoginRequest;
import com.zippy.auth.domain.entity.User;
import com.zippy.auth.exception.AppException;
import com.zippy.auth.exception.ErrorCode;
import com.zippy.auth.repository.UserRepository;
import com.zippy.auth.security.GoogleIdentityVerifier;
import com.zippy.auth.security.JwtService;
import com.zippy.auth.service.TokenService;
import com.zippy.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceUnitTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final TokenService tokens = mock(TokenService.class);
    private final JwtService jwt = mock(JwtService.class);
    private final GoogleIdentityVerifier google = mock(GoogleIdentityVerifier.class);
    private final com.zippy.auth.security.AuthRateLimiter rateLimiter = mock(com.zippy.auth.security.AuthRateLimiter.class);
    private AuthServiceImpl auth;

    @BeforeEach void setup() {
        when(passwords.encode(anyString())).thenReturn("dummy-hash");
        auth = new AuthServiceImpl(users, mock(com.zippy.auth.repository.RoleRepository.class), passwords, tokens, jwt,
            new AuthProperties("unused", "test", 900, 3600, List.of()), google,
            mock(com.zippy.auth.service.OtpService.class), mock(com.zippy.auth.security.TokenBlacklist.class), rateLimiter);
    }

    @Test void rateLimitedLoginNeverChecksPasswordOrIssuesTokens() {
        doThrow(new AppException(ErrorCode.AUTH_RATE_LIMITED)).when(rateLimiter)
            .check(eq("login"), eq("test@gmail.com"), eq(10), any());
        assertThatThrownBy(() -> auth.login(new LoginRequest(" TEST@GMAIL.COM ", "Secret!123")))
            .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AUTH_RATE_LIMITED));
        verifyNoInteractions(users, tokens, jwt);
        verify(passwords, never()).matches(any(), any());
    }

    @Test void loginNormalizesEmailButPreservesPassword() {
        var user = new User(); user.setEmail("test@gmail.com"); user.setPassword("stored-hash");
        when(users.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(passwords.matches(" Exact!123 ", "stored-hash")).thenReturn(true);
        var sessionId = java.util.UUID.randomUUID();
        when(tokens.issue(user)).thenReturn(new TokenService.IssuedRefreshToken("refresh", user, sessionId));
        when(jwt.create(user, sessionId)).thenReturn("access");

        var result = auth.login(new LoginRequest("TEST@GMAIL.COM", " Exact!123 "));

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        verify(passwords).matches(" Exact!123 ", "stored-hash");
        verify(users, never()).save(any());
    }

    @Test void absentAccountStillChecksPasswordAgainstDummyHash() {
        when(users.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> auth.login(new LoginRequest("missing@gmail.com", "Secret!123")))
            .isInstanceOf(BadCredentialsException.class);
        verify(passwords).matches("Secret!123", "dummy-hash");
        verifyNoInteractions(tokens, jwt);
    }

    @Test void longIncorrectPasswordDoesNotIssueTokens() {
        assertThatThrownBy(() -> auth.login(new LoginRequest("test@gmail.com", "Đ".repeat(37))))
            .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(tokens, jwt);
        verify(passwords).matches(any(), eq("dummy-hash"));
    }

    @Test void failedGoogleVerificationNeverQueriesAccountOrIssuesTokens() {
        when(google.verify("invalid")).thenThrow(new AppException(ErrorCode.AUTH_INVALID_GOOGLE_CREDENTIAL));
        assertThatThrownBy(() -> auth.google("invalid")).isInstanceOf(AppException.class);
        verifyNoInteractions(users, tokens, jwt);
    }
}
