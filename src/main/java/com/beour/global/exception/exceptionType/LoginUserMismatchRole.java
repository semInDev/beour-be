package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;
import org.springframework.security.core.AuthenticationException;

public class LoginUserMismatchRole extends AuthenticationException {

    private final Integer errorCode;

    public LoginUserMismatchRole(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode(){
        return this.errorCode;
    }

}
