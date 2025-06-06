package com.beour.global.exception.exceptionType;

import org.springframework.security.core.AuthenticationException;

public class LoginUserNotFoundException extends AuthenticationException {
    public LoginUserNotFoundException(String message) {
        super(message);
    }
}
