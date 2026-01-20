package com.backend.global.exception.domain;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "AUTH_400_1", "유효하지 않은 회원 정보입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_400_2", "비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_400_3", "유효하지 않은 토큰입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "로그인이 필요합니다."),
    NOT_MATCH_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "존재하지 않는 refreshToken 입니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "권한이 없습니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
