package com.zippy.auth.exception;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter @RequiredArgsConstructor
public enum ErrorCode {
    AUTH_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Too many authentication attempts; try again later"),
    AUTH_FLOW_INVALID(HttpStatus.BAD_REQUEST, "Phiên xác thực không hợp lệ hoặc đã hết hạn, vui lòng thực hiện lại"),
    AUTH_MAIL_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi email lúc này, vui lòng thử lại"),
    AUTH_DEPENDENCY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ xác thực tạm thời không khả dụng"),
    AUTH_GOOGLE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Google login chưa được cấu hình"),
    AUTH_INVALID_GOOGLE_CREDENTIAL(HttpStatus.UNAUTHORIZED, "Google credential không hợp lệ"),
    AUTH_GOOGLE_ACCOUNT_UNAVAILABLE(HttpStatus.CONFLICT, "Email Google chưa được đăng ký, vui lòng đăng ký tài khoản trước khi đăng nhập"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Invalid request"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác"),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required or invalid token"),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    AUTH_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Email này đã được đăng ký, vui lòng sử dụng email khác hoặc đăng nhập"),
    AUTH_OTP_EXPIRED(HttpStatus.BAD_REQUEST, "Mã OTP đã hết hạn, vui lòng gửi lại mã mới"),
    AUTH_INVALID_OTP(HttpStatus.BAD_REQUEST, "OTP không chính xác"),
    AUTH_OTP_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi 60 giây trước khi gửi lại OTP"),
    AUTH_RESET_SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "Phiên làm việc đã hết hạn, vui lòng thực hiện lại"),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Chức năng chưa được triển khai"),
    AUTH_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "Email của bạn không chính xác (hoặc chưa được đăng ký)"),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "Resource already exists"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid refresh token"),
    AUTH_EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh token expired"),
    AUTH_REVOKED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh token revoked"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    private final HttpStatus status;
    private final String message;
}
