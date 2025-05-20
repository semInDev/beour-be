package com.beour.global.exception;

import com.beour.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
    String message = fieldError.getDefaultMessage();

    return ResponseEntity.badRequest()
        .body(new ErrorResponse("VALIDATION_ERROR", message));
  }

}
