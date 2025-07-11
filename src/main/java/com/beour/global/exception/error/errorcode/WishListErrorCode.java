package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WishListErrorCode implements ErrorCode {

    ALREADY_IN_WISHLIST(409, "찜 목록에 존재하는 공간입니다."),
    EMPTY_WISHLIST(404, "찜 목록이 비어있습니다.");

    private final Integer code;
    private final String message;
}
