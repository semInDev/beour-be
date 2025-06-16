package com.beour.global.validator.implement;

import com.beour.global.validator.annotation.ValidNullableNickname;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullableNicknameValidator implements
    ConstraintValidator<ValidNullableNickname, String> {

    private static final String NICKNAME_REGEX = "^.{1,8}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return value.matches(NICKNAME_REGEX);
    }

}
