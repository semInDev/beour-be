package com.beour.global.validator.implement;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginIdValidatorTest {

    private final LoginIdValidator loginIdValidator = new LoginIdValidator();

    @DisplayName("아이디가 공백이다")
    @Test
    void fail_loginIdValueIsBlank(){
        //given
        String loginid = "";

        //when
        Boolean isValid = loginIdValidator.isValid(loginid, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("아이디가 4자리이다")
    @Test
    void fail_loginIdLengthIsFour(){
        //given
        String loginid = "four";

        //when
        Boolean isValid = loginIdValidator.isValid(loginid, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("아이디가 16자리이다")
    @Test
    void fail_loginIdLengthIsSixteen(){
        //given
        String loginid = "fourfourfourfour";

        //when
        Boolean isValid = loginIdValidator.isValid(loginid, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("아이디에 영어, 숫자가 아닌 다른 문자가 포함되어있다.")
    @Test
    void fail_loginIdWithSpecialCharacter(){
        //given
        String loginid = "four12$%";

        //when
        Boolean isValid = loginIdValidator.isValid(loginid, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("아이디는 5~15자이고 영어,숫자로만 이루어져있다.")
    @Test
    void success_loginId(){
        //given
        String loginid = "loginId12";

        //when
        Boolean isValid = loginIdValidator.isValid(loginid, null);

        //then
        assertTrue(isValid);
    }

}