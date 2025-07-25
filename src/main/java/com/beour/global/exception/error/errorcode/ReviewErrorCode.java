package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    NO_PERMISSION(401, "해당 리뷰에 대한 권한이 없습니다."),
    NO_RECENT_REVIEW(404, "최근 등록된 리뷰가 없습니다."),
    REVIEW_NOT_FOUND(404, "리뷰가 존재하지 않습니다."),
    ONLY_COMPLETED_CAN_REVIEW(400, "완료된 예약에 대해서만 리뷰를 작성할 수 있습니다."),
    REVIEW_ALREADY_EXISTS(409, "이미 해당 예약에 대한 리뷰가 작성되었습니다.");

    private final Integer code;
    private final String message;
}
