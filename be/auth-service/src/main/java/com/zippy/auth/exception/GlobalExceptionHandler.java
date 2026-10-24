package com.zippy.auth.exception;
import com.zippy.auth.base.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ApiResponse<Void>> error(ErrorCode code) {
        return ResponseEntity.status(code.getStatus()).body(ApiResponse.error(code.getMessage(), code.name()));
    }
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> app(AppException ex) { return error(ex.getErrorCode()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
        var messages = ex.getBindingResult().getAllErrors().stream()
            .map(org.springframework.context.support.DefaultMessageSourceResolvable::getDefaultMessage).toList();
        String message = java.util.stream.Stream.of(
            com.zippy.auth.constant.AuthMessages.REQUIRED,
            com.zippy.auth.constant.AuthMessages.EMAIL,
            com.zippy.auth.constant.AuthMessages.PHONE,
            com.zippy.auth.constant.AuthMessages.PASSWORD,
            com.zippy.auth.constant.AuthMessages.MISMATCH,
            com.zippy.auth.constant.AuthMessages.OTP)
            .filter(messages::contains).findFirst().orElse(ErrorCode.VALIDATION_ERROR.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(message, ErrorCode.VALIDATION_ERROR.name()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> malformed() { return error(ErrorCode.VALIDATION_ERROR); }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> credentials() { return error(ErrorCode.AUTH_INVALID_CREDENTIALS); }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> authentication() { return error(ErrorCode.AUTH_UNAUTHORIZED); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> forbidden() { return error(ErrorCode.AUTH_FORBIDDEN); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(DataIntegrityViolationException ex) {
        // Inspect only known constraint identifiers; never echo database exception details.
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String name = String.valueOf(violation.getConstraintName()).toLowerCase(java.util.Locale.ROOT);
                if (name.contains("uk_users_email")) return error(ErrorCode.AUTH_DUPLICATE_EMAIL);
            }
            cause = cause.getCause();
        }
        return error(ErrorCode.RESOURCE_CONFLICT);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> missing() { return error(ErrorCode.RESOURCE_NOT_FOUND); }
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiResponse<Void>> http(Exception ex) {
        var response = (org.springframework.web.ErrorResponse) ex;
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
            .body(ApiResponse.error("Unsupported request", "VALIDATION_ERROR"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> fallback(Exception ex) {
        log.error("Unhandled request failure: {}", ex.getClass().getName());
        return error(ErrorCode.INTERNAL_ERROR);
    }
    @ExceptionHandler({org.springframework.dao.DataAccessResourceFailureException.class,
        org.springframework.dao.QueryTimeoutException.class, org.springframework.data.redis.RedisSystemException.class})
    public ResponseEntity<ApiResponse<Void>> unavailable() { return error(ErrorCode.AUTH_DEPENDENCY_UNAVAILABLE); }
}
