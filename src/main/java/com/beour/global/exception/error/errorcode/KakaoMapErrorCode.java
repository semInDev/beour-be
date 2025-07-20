package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KakaoMapErrorCode implements ErrorCode {

    API_CALL_FAILED(500, "카카오맵 API 호출에 실패했습니다."),
    ADDRESS_NOT_FOUND(404, "주소로 좌표를 찾을 수 없습니다."),
    INVALID_ADDRESS_FORMAT(400, "올바르지 않은 주소 형식입니다.");

    private final Integer code;
    private final String message;
}
