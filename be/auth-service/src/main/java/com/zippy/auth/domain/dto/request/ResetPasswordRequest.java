package com.zippy.auth.domain.dto.request;
import com.zippy.auth.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import static com.zippy.auth.constant.AuthMessages.*;
@PasswordsMatch
public record ResetPasswordRequest(
    @Schema(minLength = 6, description = "Mật khẩu mới", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = REQUIRED) @ValidPassword String newPassword,
    @Schema(description = "Nhập lại mật khẩu mới", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = REQUIRED) String confirmNewPassword) {
    @Override public String toString() { return "ResetPasswordRequest[redacted]"; }
}
