package com.backend.global.exception.domain;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

    //login
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "POST_401_1", "로그인이 필요한 서비스입니다."),

    // POST
    POST_NO_PERMISSION(HttpStatus.FORBIDDEN, "POST_403_1", "해당 게시글에 대한 권한이 없습니다."),
    POST_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_403_2", "게시글 작성 권한이 없습니다."),
    POST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_403_3", "게시글 수정 권한이 없습니다."),
    POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_403_4", "게시글 삭제 권한이 없습니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POST_403_5", "해당 게시물에 접근 권한이 없습니다."),
    SUBSCRIPTION_REQUIRED(HttpStatus.FORBIDDEN, "POST_403_6", "구독(팔로우)이 필요한 게시글입니다."),
    PAID_SUBSCRIPTION_REQUIRED(HttpStatus.FORBIDDEN, "POST_403_7", "유료 멤버십 구독자만 열람 가능한 게시글입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404_1", "해당 게시글이 존재하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
