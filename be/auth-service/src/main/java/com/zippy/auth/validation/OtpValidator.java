package com.zippy.auth.validation;
import com.zippy.auth.constant.AuthMessages;
import jakarta.validation.*;
public class OtpValidator implements ConstraintValidator<ValidOtp, String> {
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank() || value.matches("[0-9]{1,5}")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(AuthMessages.REQUIRED).addConstraintViolation();
            return false;
        }
        return value.matches("[0-9]{6}");
    }
}
