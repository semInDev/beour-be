package com.beour.global.validator.implement;

import com.beour.global.validator.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private static final String PASSWORD_REGEX = "^(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).{8,20}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) return false;
        return value.matches(PASSWORD_REGEX);
    }
}
