package com.beour.global.validator.implement;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NicknameValidatorTest {

    private final NicknameValidator nicknameValidator = new NicknameValidator();

    @DisplayName("닉네임이 공백이다")
    @Test
    void fail_nicknameIsBlank(){
        //given
        String nickname = "";

        //when
        Boolean isValid = nicknameValidator.isValid(nickname, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("닉네임이 9자리이다")
    @Test
    void fail_nicknameLengthIsNine(){
        //given
        String nickname = "nineninee";

        //when
        Boolean isValid = nicknameValidator.isValid(nickname, null);

        //then
        assertFalse(isValid);
    }

    @DisplayName("닉네임이 1~8자리이다")
    @Test
    void success_nickname(){
        //given
        String nickname = "ninenine";

        //when
        Boolean isValid = nicknameValidator.isValid(nickname, null);

        //then
        assertTrue(isValid);
    }

}