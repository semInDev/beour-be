package com.beour.global.validator.implement;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordValidatorTest {

    private final PasswordValidator passwordValidator = new PasswordValidator();

    @DisplayName("비밀번호가 공백이다")
    @Test
    void fail_passowrdIsBlank(){
        //given
        String password = "";

        //when
        Boolean isValid = passwordValidator.isValid(password, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("비밀번호가 7자리이다")
    @Test
    void fail_passowrdLengthIsSeven(){
        //given
        String password = "sevennn";

        //when
        Boolean isValid = passwordValidator.isValid(password, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("비밀번호가 21자리이다")
    @Test
    void fail_passowrdLengthIstwentyone(){
        //given
        String password = "twentyonetwentyoneeee";

        //when
        Boolean isValid = passwordValidator.isValid(password, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("비밀번호가 21자리이다")
    @Test
    void fail_passowrdWithoutSpecialCharacter(){
        //given
        String password = "password";

        //when
        Boolean isValid = passwordValidator.isValid(password, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("비밀번호는 8~20자이고 특수문자를 포함하고 있다.")
    @Test
    void success_passowrd(){
        //given
        String passowrd = "passowrd##";

        //when
        Boolean isValid = passwordValidator.isValid(passowrd, null);

        //then
        assertTrue(isValid);
    }

}