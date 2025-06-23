package com.beour.global.validator.annotation;

import com.beour.global.validator.implement.NullableNicknameValidator;
import com.beour.global.validator.implement.NullablePhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자의 핸드폰번호 유효성을 검사하는 어노테이션입니다.
 * 공백을 허용합니다.
 */
@Documented
@Constraint(validatedBy = NullablePhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNullablePhoneNumber {
    String message() default "전화번호는 숫자만 10~11자리로 입력하세요.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
