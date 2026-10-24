package com.zippy.auth.domain.dto.request;
import com.zippy.auth.validation.ValidOtp;
import io.swagger.v3.oas.annotations.media.Schema;
public record OtpRequest(
    @Schema(description = "Mã OTP", pattern = "[0-9]{6}", minLength = 6, maxLength = 6, requiredMode = Schema.RequiredMode.REQUIRED)
    @ValidOtp String otp) {
    @Override public String toString() { return "OtpRequest[redacted]"; }
}
