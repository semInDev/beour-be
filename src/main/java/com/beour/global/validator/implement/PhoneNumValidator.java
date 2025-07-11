package com.beour.global.validator.implement;

import com.beour.global.validator.annotation.ValidPhoneNum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumValidator implements ConstraintValidator<ValidPhoneNum, String> {
    private static final String PHONE_NUMBER_REGEX = "^\\d{10,11}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) return false;
        return value.matches(PHONE_NUMBER_REGEX);
    }
}
