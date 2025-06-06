package com.beour.global.exception;

import com.beour.global.exception.exceptionType.LoginUserMismatchRole;
import com.beour.global.exception.exceptionType.LoginUserNotFoundException;
import com.beour.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JWTExceptionHandler {
    @ExceptionHandler(LoginUserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLoginUserNotFound(LoginUserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "LOGIN_USER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(LoginUserMismatchRole.class)
    public ResponseEntity<ErrorResponse> handleLoginUserRoleMismatch(LoginUserMismatchRole ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "ROLE_MISMATCH", ex.getMessage()));
    }
}
