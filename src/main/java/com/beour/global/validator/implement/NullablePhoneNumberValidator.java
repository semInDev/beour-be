package com.beour.global.validator.implement;

import com.beour.global.validator.annotation.ValidNullablePhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullablePhoneNumberValidator implements
    ConstraintValidator<ValidNullablePhoneNumber, String> {

    private static final String PHONENUMBER_REGEX = "^\\d{10,11}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return value.matches(PHONENUMBER_REGEX);
    }

}
