package com.beour.global.exception.exceptionType;

import com.beour.global.exception.error.ErrorCode;
import java.io.IOException;

public class ImageFileNotFoundException extends IOException {

    private final Integer errorCode;

    public ImageFileNotFoundException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public Integer getErrorCode() {
        return errorCode;
    }

}
