package com.backend.global.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // USER
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER_001", "이미 가입된 이메일입니다."),
    EMAIL_IN_PROGRESS_AUTHENTICATION(HttpStatus.CONFLICT, "USER_002", "이미 인증 진행 중인 이메일입니다."),
    EMAIL_SENDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "USER_003", "이메일 전송에 실패하였습니다."),
    NOT_FOUND_AUTHCODE(HttpStatus.NOT_FOUND, "USER_004", "인증 정보를 찾을 수 없습니다."),
    AUTHENTICATION_EXPIRED(HttpStatus.BAD_REQUEST, "USER_005", "인증 코드가 만료되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "USER_006", "이메일 인증이 완료되지 않았습니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER_007", "이미 사용 중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_008", "사용자를 찾을 수 없습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "USER_009", "인증번호가 일치하지 않습니다."),
    NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "USER_010", "닉네임은 필수 입력 항목입니다."),
    NICKNAME_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "USER_011", "닉네임은 공백 없이 2자 이상이어야 합니다."),

    // ROLE
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_001", "존재하지 않는 역할입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;


}
