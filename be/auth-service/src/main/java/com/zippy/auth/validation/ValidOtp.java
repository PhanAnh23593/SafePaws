package com.zippy.auth.validation;
import com.zippy.auth.constant.AuthMessages;
import jakarta.validation.*;
import java.lang.annotation.*;
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OtpValidator.class)
public @interface ValidOtp {
    String message() default AuthMessages.OTP;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
