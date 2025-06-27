package com.beour.reservation.commons.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UsagePurpose {

    GROUP_MEETING("단체 모임"),
    COOKING_PRACTICE("요리 연습"),
    BARISTA_TRAINING("바리스타 실습"),
    FLEA_MARKET("플리마켓"),
    FILMING("촬영"),
    OTHER("기타");

    private final String text;
    public static final UsagePurpose DEFAULT = OTHER;
}
