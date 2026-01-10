package com.backend.global.exception;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubscribeErrorCode implements ErrorCode {
    CANNOT_DELETE_NOT_EXPIRED(HttpStatus.BAD_REQUEST, "SUBSCRIBE-400_1", "만료되지 않은 구독은 삭제할 수 없습니다."),
    CANNOT_SUBSCRIBE_SELF(HttpStatus.BAD_REQUEST, "SUBSCRIBE-400_2", "본인은 구독할 수 없습니다."),

    FORBIDDEN_SUBSCRIBE(HttpStatus.FORBIDDEN, "SUBSCRIBE-403", "해당 구독에 대한 권한이 없습니다."),

    NOT_FOUND_SUBSCRIBE(HttpStatus.NOT_FOUND, "SUBSCRIBE-404_1", "구독 정보를 찾을 수 없습니다."),
    NOT_FOUND_CREATOR(HttpStatus.NOT_FOUND, "SUBSCRIBE-404_2", "크리에이터를 찾을 수 없습니다."),

    ALREADY_SUBSCRIBED(HttpStatus.CONFLICT, "SUBSCRIBE-409_1", "이미 구독 중입니다."),
    NOT_EXPIRED_CANNOT_DELETE(HttpStatus.CONFLICT, "SUBSCRIBE-409_2", "만료되지 않은 구독은 삭제할 수 없습니다.");







    private final HttpStatus status;
    private final String code;
    private final String message;

}
