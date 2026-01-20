package com.backend.global.exception.domain;

import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    //400 Bad Request: 클라이언트의 요청 데이터가 잘못됨
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_400_1", "주문 금액과 결제 금액이 일치하지 않습니다."),
    INVALID_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "PAYMENT_400_2", "유효하지 않은 결제 키입니다."),

    //409 Conflict: 상태 충돌 (중복 결제 등)
    ALREADY_PAID(HttpStatus.CONFLICT, "PAYMENT_409", "이미 결제가 완료된 주문입니다."),

    //500 Internal Server Error: 결제 취소(보상 트랜잭션) 실패 - 긴급 조치 필요
    PAYMENT_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500", "결제 취소 요청에 실패했습니다. 관리자에게 문의하세요."),

    // 500, 502 Internal Server Error: 서버 내부 또는 외부 API(토스) 문제
    PAYMENT_CONFIRM_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_502", "토스페이먼츠 결제 승인에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}