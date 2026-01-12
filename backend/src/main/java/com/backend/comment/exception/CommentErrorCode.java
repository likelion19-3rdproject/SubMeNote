package com.backend.comment.exception;

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

    //todo 도메인 중복 에러같은데... 일단 임시로 여기에 만들어놓겠습니다
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404", "게시글을 찾을 수 없습니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POST_403", "해당 게시글에 접근할 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}