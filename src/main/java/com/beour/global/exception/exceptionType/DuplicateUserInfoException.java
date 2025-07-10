package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class DuplicateUserInfoException extends RuntimeException {
    private final Integer errorCode;

    public DuplicateUserInfoException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public Integer getErrorCode(){
        return errorCode;
    }
}
