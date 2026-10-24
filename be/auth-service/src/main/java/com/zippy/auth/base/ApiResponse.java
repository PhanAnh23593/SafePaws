package com.zippy.auth.base;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data, String errorCode) {
    public static <T> ApiResponse<T> ok(String message, T data) { return new ApiResponse<>(true, message, data, null); }
    public static ApiResponse<Void> error(String message, String code) { return new ApiResponse<>(false, message, null, code); }
}
