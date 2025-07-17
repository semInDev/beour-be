package com.beour.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenExpireTime {

    //10분  1000L * 60 * 10
    ACCESS_TOKEN_EXPIRATION_MILLIS(1000L * 60 * 10, "access token 만료 시간"),
    //1일  1000L * 60 * 60 * 24
    REFRESH_TOKEN_EXPIRATION_MILLIS(1000L * 60 * 60 * 24, "refresh token 만료 시간");

    private final long value;
    private final String description;
}
