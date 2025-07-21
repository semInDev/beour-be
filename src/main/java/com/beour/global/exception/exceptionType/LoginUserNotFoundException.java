package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;
import org.springframework.security.core.AuthenticationException;

public class LoginUserNotFoundException extends AuthenticationException {
    private final Integer errorCode;

    public LoginUserNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public Integer getErrorCode(){
        return this.errorCode;
    }
}
