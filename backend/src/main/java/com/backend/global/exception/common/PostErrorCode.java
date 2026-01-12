package com.backend.global.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {
    // POST
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "해당 게시글이 존재하지 않습니다."),
    POST_NO_PERMISSION(HttpStatus.FORBIDDEN, "POST_002", "해당 게시글에 대한 권한이 없습니다."),
    POST_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_003", "게시글 작성 권한이 없습니다."),
    POST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_004", "게시글 수정 권한이 없습니다."),
    POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_005", "게시글 삭제 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_006", "해당 사용자가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return null;
    }
}
