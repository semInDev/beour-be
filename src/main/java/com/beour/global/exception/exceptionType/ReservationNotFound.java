package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class ReservationNotFound extends RuntimeException{

    private final Integer errorCode;

    public ReservationNotFound(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public Integer getErrorCode(){
        return this.errorCode;
    }

}
