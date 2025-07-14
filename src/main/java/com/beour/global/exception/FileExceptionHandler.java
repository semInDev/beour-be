package com.beour.global.exception;

import com.beour.global.exception.exceptionType.ImageFileInvalidException;
import com.beour.global.exception.exceptionType.ImageFileNotFoundException;
import com.beour.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler(ImageFileNotFoundException.class)
    public ResponseEntity<ErrorResponse> fileNotFound(
        ImageFileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getErrorCode(), "IMAGE_FILE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ImageFileInvalidException.class)
    public ResponseEntity<ErrorResponse> fileNotFound(
        ImageFileInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getErrorCode(), "IMAGE_FILE_INVALID", ex.getMessage()));
    }
}
