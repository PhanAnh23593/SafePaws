package com.zippy.auth.controller;
import com.zippy.auth.base.ApiResponse;
import com.zippy.auth.constant.UrlConstants;
import com.zippy.auth.domain.dto.request.*;
import com.zippy.auth.domain.dto.response.*;
import com.zippy.auth.security.UserPrincipal;
import com.zippy.auth.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, OTP, recovery and token lifecycle. Preserve X-Auth-Flow response header for OTP and reset requests.")
public class AuthController {
    private final AuthService auth;
    @PostMapping(UrlConstants.REGISTER) @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request, jakarta.servlet.http.HttpServletResponse response) {
        var started = auth.register(request);
        response.setHeader("X-Auth-Flow", started.flowToken());
        response.setHeader("Cache-Control", "no-store");
        return ApiResponse.ok("Đã gửi mã OTP", started.user());
    }
    @PostMapping(UrlConstants.LOGIN)
    @io.swagger.v3.oas.annotations.Operation(summary = "Đăng nhập bằng Email / Gmail và Mật khẩu")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Thiếu thông tin, email sai định dạng hoặc trường không được phép"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không chính xác")})
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successfully", auth.login(request));
    }
    @PostMapping(UrlConstants.REGISTER_VERIFY_OTP)
    public ApiResponse<Void> verifyRegistration(@Valid @RequestBody OtpRequest request,
            @RequestHeader(value = "X-Auth-Flow", required = false) String flow) {
        auth.verifyRegistration(flow, request); return ApiResponse.ok("Xác thực thành công", null);
    }
    @PostMapping(UrlConstants.REGISTER_RESEND_OTP)
    public ApiResponse<Void> resendRegistrationOtp(@RequestHeader(value = "X-Auth-Flow", required = false) String flow) {
        auth.resendRegistrationOtp(flow); return ApiResponse.ok("Đã gửi mã OTP", null);
    }
    @PostMapping(UrlConstants.FORGOT_PASSWORD)
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("X-Auth-Flow", auth.forgotPassword(request));
        response.setHeader("Cache-Control", "no-store");
        return ApiResponse.ok("Nếu email đã được đăng ký, bạn sẽ nhận được mã xác thực", null);
    }
    @PostMapping(UrlConstants.FORGOT_PASSWORD_VERIFY_OTP)
    public ApiResponse<Void> verifyForgotPassword(@Valid @RequestBody OtpRequest request,
            @RequestHeader(value = "X-Auth-Flow", required = false) String flow) {
        auth.verifyForgotPassword(flow, request); return ApiResponse.ok("Xác thực thành công", null);
    }
    @PostMapping(UrlConstants.FORGOT_PASSWORD_RESEND_OTP)
    public ApiResponse<Void> resendForgotPasswordOtp(@RequestHeader(value = "X-Auth-Flow", required = false) String flow) {
        auth.resendForgotPasswordOtp(flow); return ApiResponse.ok("Nếu email đã được đăng ký, bạn sẽ nhận được mã xác thực", null);
    }
    @PostMapping(UrlConstants.RESET_PASSWORD)
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
            @RequestHeader(value = "X-Auth-Flow", required = false) String flow) {
        auth.resetPassword(flow, request); return ApiResponse.ok("Đổi mật khẩu thành công", null);
    }
    @PostMapping(UrlConstants.GOOGLE)
    @io.swagger.v3.oas.annotations.Operation(summary = "Đăng nhập bằng Google", description = "Exchange Google ID token for internal tokens. Existing accounts only; no extra screen fields.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid technical request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid Google credential"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email chưa đăng ký: chuyển đến form đăng ký hiện có, giữ nguyên 5 trường"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Google client IDs not configured")})
    public ApiResponse<AuthResponse> google(@Valid @RequestBody GoogleCredentialRequest request) {
        return ApiResponse.ok("Login successfully", auth.google(request.credential()));
    }
    @PostMapping(UrlConstants.REFRESH)
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed", auth.refresh(request.refreshToken()));
    }
    @PostMapping(UrlConstants.LOGOUT)
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.Operation(summary = "Logout current session", description = "Send only the Bearer access token. Access and refresh tokens for this session are revoked; no request body is required.")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        auth.logout(com.zippy.auth.util.BearerTokenUtils.extract(authorization));
        return ApiResponse.ok("Logged out successfully", null);
    }
    @GetMapping(UrlConstants.ME) @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok("Current user", auth.me(principal.id()));
    }
}
