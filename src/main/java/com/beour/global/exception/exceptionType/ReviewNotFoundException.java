package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class ReviewNotFoundException extends RuntimeException {

    private final Integer errorCode;

    public ReviewNotFoundException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public Integer getErrorCode() {
        return this.errorCode;
    }
}
