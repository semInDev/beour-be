package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AvailableTimeErrorCode implements ErrorCode {

    AVAILABLE_TIME_NOT_FOUND(404, "예약 가능한 시간이 존재하지 않습니다."),
    TIME_UNAVAILABLE(400, "예약이 불가능한 시간입니다.");

    private final Integer code;
    private final String message;
}
