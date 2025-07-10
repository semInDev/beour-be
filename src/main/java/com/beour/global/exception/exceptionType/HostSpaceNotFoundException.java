package com.beour.global.exception.exceptionType;

public class HostSpaceNotFoundException extends RuntimeException {
    public HostSpaceNotFoundException() {
        super("해당 호스트가 등록한 공간이 없습니다.");
    }
}
