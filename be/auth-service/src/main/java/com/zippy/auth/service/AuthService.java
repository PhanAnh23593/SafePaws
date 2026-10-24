package com.zippy.auth.service;
import com.zippy.auth.domain.dto.request.*;
import com.zippy.auth.domain.dto.response.*;
import java.util.UUID;
public interface AuthService {
    record RegistrationStarted(UserResponse user, String flowToken) {
        @Override public String toString() { return "RegistrationStarted[redacted]"; }
    }
    RegistrationStarted register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String rawToken);
    void logout(String accessToken);
    UserResponse me(UUID id);
    void verifyRegistration(String flow, OtpRequest request);
    void resendRegistrationOtp(String flow);
    String forgotPassword(ForgotPasswordRequest request);
    void verifyForgotPassword(String flow, OtpRequest request);
    void resendForgotPasswordOtp(String flow);
    void resetPassword(String flow, ResetPasswordRequest request);
    AuthResponse google(String credential);
}
