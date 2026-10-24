package com.zippy.auth.validation;
import jakarta.validation.*;
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    @Override public boolean isValid(String value, ConstraintValidatorContext context) {
        // Required input is handled by NotBlank, with its own exact message.
        return value == null || value.isBlank() || (value.codePointCount(0, value.length()) >= 6
            && value.codePoints().anyMatch(Character::isUpperCase)
            && value.codePoints().anyMatch(c -> !Character.isLetterOrDigit(c)
                && !Character.isWhitespace(c) && !Character.isSpaceChar(c)));
    }
}
