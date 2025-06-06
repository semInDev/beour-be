package com.beour.global.exception.exceptionType;

import org.springframework.security.core.AuthenticationException;

public class LoginUserMismatchRole extends AuthenticationException {

    public LoginUserMismatchRole(String message) {
        super(message);
    }

}
