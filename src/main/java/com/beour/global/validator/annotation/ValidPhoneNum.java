package com.beour.global.validator.annotation;

import com.beour.global.validator.implement.LoginIdValidator;
import com.beour.global.validator.implement.PhoneNumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자의 핸드폰 번호 유효성을 검사하는 어노테이션입니다.
 */
@Documented
@Constraint(validatedBy = PhoneNumValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNum {

    String message() default "전화번호는 숫자만 10~11자리로 입력하세요.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
