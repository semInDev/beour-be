package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    NO_INFO_TO_UPDATE(400, "수정할 정보를 입력해주세요.");

    private final Integer code;
    private final String message;
}
