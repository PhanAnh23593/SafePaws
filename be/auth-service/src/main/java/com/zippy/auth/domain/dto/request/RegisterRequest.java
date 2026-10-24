package com.zippy.auth.domain.dto.request;
import com.zippy.auth.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import static com.zippy.auth.constant.AuthMessages.*;
@PasswordsMatch
public record RegisterRequest(
    @Schema(description = "Họ và tên / Tên đăng nhập")
    @NotBlank(message = REQUIRED) String fullName,
    @Schema(description = "Email / Gmail")
    @NotBlank(message = REQUIRED) @Email(message = EMAIL) @Size(max = 254, message = EMAIL) String email,
    @Schema(description = "Số điện thoại")
    @NotBlank(message = REQUIRED) @Pattern(regexp = "[0-9]{10}", message = PHONE) String phone,
    @Schema(minLength = 6, description = "Mật khẩu", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = REQUIRED) @ValidPassword String password,
    @Schema(description = "Nhập lại mật khẩu", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = REQUIRED) String confirmPassword) {
    @Override public String toString() { return "RegisterRequest[redacted]"; }
}
