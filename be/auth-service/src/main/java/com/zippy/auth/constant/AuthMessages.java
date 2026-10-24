package com.zippy.auth.constant;
public final class AuthMessages {
    private AuthMessages() {}
    public static final String REQUIRED = "Vui lòng nhập đầy đủ thông tin";
    public static final String EMAIL = "Email không đúng định dạng";
    public static final String PHONE = "Số điện thoại không hợp lệ (vui lòng nhập đủ 10 chữ số)";
    public static final String PASSWORD = "Mật khẩu phải có từ 6 kí tự trở lên, bao gồm kí tự in hoa và kí tự đặc biệt";
    public static final String MISMATCH = "Mật khẩu không trùng khớp";
    public static final String OTP = "OTP không chính xác";
}
