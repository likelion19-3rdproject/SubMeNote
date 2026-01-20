package com.backend.global.exception.domain;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {

    SETTLEMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "SETTLEMENT_403_1", "해당 정산 내역 조회 권한이 없습니다."),
    NO_PENDING_SETTLEMENT(HttpStatus.FORBIDDEN, "SETTLEMENT_403_2", "정산할 내역이 없습니다."),

    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_404", "정산 내역을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
