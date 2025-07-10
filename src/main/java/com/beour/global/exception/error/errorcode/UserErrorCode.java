package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(404, "존재하지 않는 유저입니다."),
    LOGIN_ID_DUPLICATE(409, "이미 사용중인 아이디입니다."),
    NICKNAME_ID_DUPLICATE(409, "이미 사용중인 닉네임입니다.");


    private final Integer code;
    private final String message;
}
