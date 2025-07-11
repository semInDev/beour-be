package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class DuplicateException extends RuntimeException{
    private final Integer errorCode;

    public DuplicateException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode(){
        return this.errorCode;
    }
}
