package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class KakaoMapException extends RuntimeException {

    private final Integer errorCode;

    public KakaoMapException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
