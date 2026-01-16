package com.backend.global.exception;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {

    ALREADY_REPORTED(HttpStatus.CONFLICT, "REPORT_409_1", "이미 신고한 대상입니다."),
    CANNOT_REPORT_SELF(HttpStatus.BAD_REQUEST, "REPORT_400_1", "본인이 작성한 대상은 신고할 수 없습니다."),

    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404", "신고 대상이 존재하지 않습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404_1", "신고 내역을 찾을 수 없습니다."),

    NOT_REPORTED_OBJECT(HttpStatus.NOT_FOUND, "REPORT_409_2", "신고되지 않은 게시물입니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}
