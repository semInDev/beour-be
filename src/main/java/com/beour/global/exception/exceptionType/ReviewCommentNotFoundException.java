package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class ReviewCommentNotFoundException extends RuntimeException {
    private final Integer errorCode;

    public ReviewCommentNotFoundException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public Integer getErrorCode(){
        return this.errorCode;
    }
}
