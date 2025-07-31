package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(404, "존재하지 않는 유저입니다."),
    MEMBER_NOT_FOUND(404, "일치하는 회원을 찾을 수 없습니다."),
    LOGIN_ID_DUPLICATE(409, "이미 사용중인 아이디입니다."),
    NICKNAME_ID_DUPLICATE(409, "이미 사용중인 닉네임입니다."),
    USER_ROLE_MISMATCH(400, "역할이 일치하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(450, "access 토큰이 만료되었습니다."),
    NOT_ACCESS_TOKEN(401, "access 토큰이 아닙니다."),
    ACCESS_TOKEN_NOT_FOUND(404, "access 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(451, "refresh 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(404, "refresh 토큰이 존재하지 않습니다.");


    private final Integer code;
    private final String message;
}
