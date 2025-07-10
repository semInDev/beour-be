package com.beour.global.exception.error.errorcode;

import com.beour.global.exception.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {

    COMMENT_ALREADY_EXISTS(409, "이미 해당 리뷰에 대한 댓글이 작성되었습니다."),
    UNAUTHORIZED_COMMENT(401, "해당 댓글에 대한 권한이 없습니다.");

    private final Integer code;
    private final String message;
}
