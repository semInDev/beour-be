package com.beour.global.exception;

import com.beour.global.exception.exceptionType.DuplicateException;
import com.beour.global.exception.exceptionType.InputInvalidFormatException;
import com.beour.global.exception.exceptionType.ReviewCommentNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.response.ErrorResponse;
import com.beour.global.exception.exceptionType.MissMatch;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
        String message = fieldError.getDefaultMessage();

        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR", message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleEnumBindingException(
        MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ENUM", "유효하지 않은 값입니다"));
    }

    @ExceptionHandler(InputInvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInputFormat(InputInvalidFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getErrorCode(), "INPUT_INVALID_FORMAT", ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
        ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().iterator().next().getMessage();
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(MissMatch.class)
    public ResponseEntity<ErrorResponse> handleMissMatch(MissMatch ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getErrorCode(), "MISS_MATCH", ex.getMessage()));
    }

    @ExceptionHandler(ReviewCommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewCommentNotFound(
        ReviewCommentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "REVIEW_COMMENT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorityException.class)
    public ResponseEntity<ErrorResponse> invalidFormat(UnauthorityException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(ex.getErrorCode(), "UNAUTHORIZED_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(ex.getErrorCode(), "DUPLICATE_ERROR", ex.getMessage()));
    }
}
