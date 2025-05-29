package com.beour.global.exception;

import com.beour.global.exception.exceptionType.InputNotFoundException;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.response.ErrorResponse;
import java.util.InputMismatchException;
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
        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", message));
  }

  @ExceptionHandler(InputNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSpaceNotFound(InputNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "REQUEST_NOT_FOUND", ex.getMessage()));
  }


}
