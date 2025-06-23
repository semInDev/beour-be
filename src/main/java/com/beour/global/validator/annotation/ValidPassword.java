package com.beour.global.validator.annotation;

import com.beour.global.validator.implement.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자의 비밀번호 유효성을 검사하는 어노테이션입니다.
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
