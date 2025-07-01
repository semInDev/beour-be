package com.beour.global.validator.implement;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NullableNicknameValidatorTest {

    private final NullableNicknameValidator nullableNicknameValidator = new NullableNicknameValidator();

    @Test
    @DisplayName("닉네임이 공백이다.")
    void nickname_blank() {
        //given
        String nickname = "";

        //when
        Boolean isValid = nullableNicknameValidator.isValid(nickname, null);

        //then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("닉네임이 9자이다.")
    void nickname_length_nine() {
        //given
        String nickname = "123456789";

        //when
        Boolean isValid = nullableNicknameValidator.isValid(nickname, null);

        //then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("닉네임이 1~8자리 사이이다.")
    void nickname_lenght_one_to_eight() {
        //given
        String nickname = "12345678";

        //when
        Boolean isValid = nullableNicknameValidator.isValid(nickname, null);

        //then
        assertTrue(isValid);
    }

}