package com.backend.global.exception.domain;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode { //원격에서 global 패키지에있는 익셉션 참고

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT_403", "댓글에 대한 권한이 없습니다."),
    COMMENT_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "COMMENT_400_1", "댓글 내용은 비어있을 수 없습니다."),
    COMMENT_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "COMMENT_400_2", "댓글 내용이 너무 깁니다."),
    REPLY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT_400_3", "대댓글에는 더 이상 댓글을 작성할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}