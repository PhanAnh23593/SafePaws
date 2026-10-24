package com.zippy.auth.validation;
import com.zippy.auth.domain.dto.request.*;
import jakarta.validation.*;
public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, Object> {
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String password;
        String confirmation;
        if (value instanceof RegisterRequest request) {
            password = request.password(); confirmation = request.confirmPassword();
        } else if (value instanceof ResetPasswordRequest request) {
            password = request.newPassword(); confirmation = request.confirmNewPassword();
        } else { return true; }
        return password == null || password.isBlank() || confirmation == null
            || confirmation.isBlank() || password.equals(confirmation);
    }
}
