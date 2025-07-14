package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageFileErrorCode implements ErrorCode {

    IMAGE_NOT_FOUND(404, "이미지 파일이 존재하지 않습니다."),
    NO_IMAGE_FILE(400, "이미지 파일이 비어있습니다.");



    private final Integer code;
    private final String message;
}
