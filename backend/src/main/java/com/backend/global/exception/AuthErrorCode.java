package com.backend.global.exception;

import com.backend.global.exception.common.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    INVALID_CREDENTIALS(
            HttpStatus.BAD_REQUEST,
            "AUTH_400",
            "유효하지 않은 회원 정보입니다."
    ),

    INVALID_BODY(
            HttpStatus.BAD_REQUEST,
            "COMMON_401",
            "요청 본문이 올바르지 않습니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON_500",
            "서버 오류가 발생했습니다."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
