package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class UnauthorityException extends RuntimeException {

    private final Integer errorCode;

    public UnauthorityException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode() {
        return this.errorCode;
    }
}
