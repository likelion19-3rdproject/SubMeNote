package com.backend.global.exception;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // USER
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "USER_001", "이메일 인증이 완료되지 않았습니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_003", "사용자를 찾을 수 없습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "USER_004", "인증번호가 일치하지 않습니다."),
    NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "USER_005", "닉네임은 필수 입력 항목입니다."),
    NICKNAME_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "USER_011", "닉네임은 공백 없이 2자 이상이어야 합니다."),
    CREATOR_HAS_ACTIVE_SUBSCRIBERS(HttpStatus.BAD_REQUEST, "USER_012", "활성 구독자가 있어 탈퇴할 수 없습니다."),
    USER_HAS_PAID_SUBSCRIPTIONS(HttpStatus.BAD_REQUEST, "USER_013", "유료 구독 중인 크리에이터가 있어 탈퇴할 수 없습니다."),
    CREATOR_FORBIDDEN(HttpStatus.FORBIDDEN, "USER_014", "계좌 정보는 크리에이터만 접근할 수 있습니다."),
    ACCOUNT_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "USER_015", "이미 등록된 계좌가 존재합니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_016", "등록된 계좌를 찾을 수 없습니다."),

    // ROLE
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_001", "존재하지 않는 역할입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
