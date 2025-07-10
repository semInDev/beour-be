package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND(404, "리뷰가 존재하지 않습니다."),
    REVIEW_ALREADY_EXISTS(409, "이미 해당 예약에 대한 리뷰가 작성되었습니다.");

    private final Integer code;
    private final String message;
}
