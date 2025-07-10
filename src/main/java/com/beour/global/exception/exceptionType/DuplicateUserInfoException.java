package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class DuplicateUserInfoException extends RuntimeException {
    private final Integer errorCode;

    public DuplicateUserInfoException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode(){
        return errorCode;
    }
}
