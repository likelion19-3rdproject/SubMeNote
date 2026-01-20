package com.backend.global.exception.domain;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {

    REPLY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT_400", "대댓글에는 더 이상 댓글을 작성할 수 없습니다."),

    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT_403", "댓글에 대한 권한이 없습니다."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "댓글을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}