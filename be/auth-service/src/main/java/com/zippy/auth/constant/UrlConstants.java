package com.zippy.auth.constant;

/** Centralized API paths and infrastructure URL patterns. */
public final class UrlConstants {
    public static final String AUTH_BASE = "/api/auth";
    public static final String REGISTER = AUTH_BASE + "/register";
    public static final String REGISTER_VERIFY_OTP = REGISTER + "/verify-otp";
    public static final String REGISTER_RESEND_OTP = REGISTER + "/resend-otp";
    public static final String LOGIN = AUTH_BASE + "/login";
    public static final String GOOGLE = AUTH_BASE + "/google";
    public static final String FORGOT_PASSWORD = AUTH_BASE + "/forgot-password";
    public static final String FORGOT_PASSWORD_VERIFY_OTP = FORGOT_PASSWORD + "/verify-otp";
    public static final String FORGOT_PASSWORD_RESEND_OTP = FORGOT_PASSWORD + "/resend-otp";
    public static final String RESET_PASSWORD = AUTH_BASE + "/reset-password";
    public static final String REFRESH = AUTH_BASE + "/refresh";
    public static final String LOGOUT = AUTH_BASE + "/logout";
    public static final String ME = AUTH_BASE + "/me";

    public static final String HEALTH = "/actuator/health";
    public static final String LIVENESS = HEALTH + "/liveness";
    public static final String READINESS = HEALTH + "/readiness";
    public static final String SWAGGER_UI = "/swagger-ui.html";
    public static final String SWAGGER_UI_RESOURCES = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs";
    public static final String API_DOCS_RESOURCES = "/v3/api-docs/**";
    public static final String ALL_PATHS = "/**";

    private UrlConstants() {}
}
