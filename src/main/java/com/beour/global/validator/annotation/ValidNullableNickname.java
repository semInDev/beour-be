package com.beour.global.validator.annotation;

import com.beour.global.validator.implement.NullableNicknameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자의 닉네임 유효성을 검사하는 어노테이션입니다.
 * 공백을 허용합니다.
 */
@Documented
@Constraint(validatedBy = NullableNicknameValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNullableNickname {
    String message() default "닉네임은 1자 이상 8자 이하여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
