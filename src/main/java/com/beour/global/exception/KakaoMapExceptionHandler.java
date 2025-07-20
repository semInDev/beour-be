package com.beour.global.exception;

import com.beour.global.exception.exceptionType.KakaoMapException;
import com.beour.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class KakaoMapExceptionHandler {

    @ExceptionHandler(KakaoMapException.class)
    public ResponseEntity<ErrorResponse> handleKakaoMapException(KakaoMapException ex) {
        log.error("KakaoMap API 오류 발생: {}", ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex.getErrorCode());

        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getErrorCode(), determineErrorType(ex.getErrorCode()), ex.getMessage()));
    }

    private HttpStatus determineHttpStatus(Integer errorCode) {
        if (errorCode == 404) {
            return HttpStatus.NOT_FOUND;
        } else if (errorCode == 400) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private String determineErrorType(Integer errorCode) {
        if (errorCode == 404) {
            return "ADDRESS_NOT_FOUND";
        } else if (errorCode == 400) {
            return "INVALID_ADDRESS";
        } else {
            return "KAKAO_API_ERROR";
        }
    }
}
