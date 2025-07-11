package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class InputInvalidFormatException extends RuntimeException {
    private final Integer errorCode;

    public InputInvalidFormatException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public Integer getErrorCode(){
        return this.errorCode;
    }

}
