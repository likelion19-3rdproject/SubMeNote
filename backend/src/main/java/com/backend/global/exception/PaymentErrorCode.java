package com.backend.global.exception;
import com.backend.global.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    // 400 Bad Request: 클라이언트의 요청 데이터가 잘못됨
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_400_1", "주문 금액과 결제 금액이 일치하지 않습니다."),
    INVALID_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "PAYMENT_400_2", "유효하지 않은 결제 키입니다."),
    PAYMENT_CANCELED_BY_USER(HttpStatus.BAD_REQUEST, "PAYMENT_400_3", "사용자가 결제를 취소했습니다."),

    // 404 Not Found: 데이터를 찾을 수 없음
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_1", "결제 내역을 찾을 수 없습니다."),

    // 409 Conflict: 상태 충돌 (중복 결제 등)
    ALREADY_PAID(HttpStatus.CONFLICT, "PAYMENT_409_1", "이미 결제가 완료된 주문입니다."),
    ALREADY_CANCELED(HttpStatus.CONFLICT, "PAYMENT_409_2", "이미 취소된 결제입니다."),

    // 500, 502 Internal Server Error: 서버 내부 또는 외부 API(토스) 문제
    PAYMENT_CONFIRM_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_502_1", "토스페이먼츠 결제 승인에 실패했습니다."),
    PAYMENT_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_1", "결제 처리 중 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}