package com.backend.global.exception;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MailErrorCode implements ErrorCode {
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MAIL_001", "이미 가입된 이메일입니다."),
    EMAIL_IN_PROGRESS_AUTHENTICATION(HttpStatus.CONFLICT, "MAIL_002", "이미 인증 진행 중인 이메일입니다."),
    EMAIL_SENDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_003", "이메일 전송에 실패하였습니다."),
    NOT_FOUND_AUTHCODE(HttpStatus.NOT_FOUND, "MAIL_004", "인증 정보를 찾을 수 없습니다."),
    AUTHENTICATION_EXPIRED(HttpStatus.BAD_REQUEST, "MAIL_005", "인증 코드가 만료되었습니다."),
    WRONG_FORMAT_CODE(HttpStatus.BAD_REQUEST, "MAIL_006", "인증코드는 6자리 숫자입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
