package com.beour.global.exception;

import com.beour.global.exception.exceptionType.DuplicateUserInfoException;
import com.beour.global.exception.exceptionType.InvalidCredentialsException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "INVALID_CREDENTIALS",
                ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                new ErrorResponse(ex.getErrorCode(), "USER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateUserInfoException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserInfo(DuplicateUserInfoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(ex.getErrorCode(), "DUPLICATE_USER_INFO", ex.getMessage()));
    }
}
