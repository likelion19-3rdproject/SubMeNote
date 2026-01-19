package com.backend.global.exception.domain;

import com.backend.global.exception.common.ErrorCode;
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
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POST_006", "해당 게시물에 접근 권한이 없습니다."),
    SUBSCRIPTION_REQUIRED(HttpStatus.FORBIDDEN, "POST_007", "구독(팔로우)이 필요한 게시글입니다."),
    PAID_SUBSCRIPTION_REQUIRED(HttpStatus.FORBIDDEN, "POST_008", "유료 멤버십 구독자만 열람 가능한 게시글입니다."),

    //login
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "POST_009", "로그인이 필요한 서비스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
