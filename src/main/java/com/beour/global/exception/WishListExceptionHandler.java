package com.beour.global.exception;

import com.beour.global.exception.exceptionType.DuplicateLikesException;
import com.beour.global.exception.exceptionType.DuplicateUserInfoException;
import com.beour.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WishListExceptionHandler {
    @ExceptionHandler(DuplicateLikesException.class)
    public ResponseEntity<com.beour.global.response.ErrorResponse> handleDuplicateLikes(DuplicateLikesException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT.value(), "DUPLICATE_LIKES", ex.getMessage()));
    }
}
