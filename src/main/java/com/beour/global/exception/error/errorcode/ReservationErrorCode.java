package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    NO_PERMISSION(401, "해당 예약에 대한 권한이 없습니다."),
    INVALID_CAPACITY(400, "해당 인원은 예약이 불가능합니다."),
    INVALID_PRICE(400, "해당 가격이 맞지 않습니다."),
    CANNOT_CANCEL_RESERVATION(400, "해당 예약은 취소할 수 없습니다."),
    SPACE_MISMATCH(400, "예약과 공간 정보가 일치하지 않습니다."),
    RESERVATION_NOT_FOUND(404, "예약이 존재하지 않습니다.");

    private final Integer code;
    private final String message;

}
