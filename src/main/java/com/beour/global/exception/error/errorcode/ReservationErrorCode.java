package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    NO_PERMISSION(401, "해당 예약에 대한 권한이 없습니다."),
    RESERVATION_NOT_FOUND(404, "예약이 존재하지 않습니다.");

    private final Integer code;
    private final String message;

}
