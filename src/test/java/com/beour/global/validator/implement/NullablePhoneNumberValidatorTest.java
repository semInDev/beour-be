package com.beour.global.validator.implement;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NullablePhoneNumberValidatorTest {

    private final NullablePhoneNumberValidator nullablePhoneNumberValidator = new NullablePhoneNumberValidator();

    @DisplayName("핸드폰 번호가 공백이다")
    @Test
    void phone_number_blank(){
        //given
        String phoneNum = "";

        //when
        Boolean isValid = nullablePhoneNumberValidator.isValid(phoneNum, null);

        //then
        assertTrue(isValid);
    }

    @DisplayName("핸드폰 번호가 9자리이다")
    @Test
    void fail_phone_number_length_nine(){
        //given
        String phoneNum = "010111111";

        //when
        Boolean isValid = nullablePhoneNumberValidator.isValid(phoneNum, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("핸드폰 번호가 12자리이다")
    @Test
    void fail_phone_number_length_twelve(){
        //given
        String phoneNum = "010111111111";

        //when
        Boolean isValid = nullablePhoneNumberValidator.isValid(phoneNum, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("핸드폰 번호가 10자리이다")
    @Test
    void phone_number_length_ten(){
        //given
        String phoneNum = "0101234567";

        //when
        Boolean isValid = nullablePhoneNumberValidator.isValid(phoneNum, null);

        //then
        assertTrue(isValid);
    }
}