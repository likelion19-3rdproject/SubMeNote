package com.backend.global.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "해당 댓글이 존재하지 않습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT_002", "해당 댓글에 대한 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return null;
    }
}
