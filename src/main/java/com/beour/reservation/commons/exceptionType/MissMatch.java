package com.beour.reservation.commons.exceptionType;

import com.beour.global.exception.error.ErrorCode;

public class MissMatch extends RuntimeException {

    private final Integer errorCode;

    public MissMatch(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public Integer getErrorCode(){
        return this.errorCode;
    }

}
