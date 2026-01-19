package com.backend.payment.service;

import com.backend.payment.dto.PaymentConfirmRequest;
import com.backend.payment.dto.PaymentResponse;
import com.backend.payment.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentService paymentService;

    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        // 1. [외부 연동] 결제 승인 요청 (트랜잭션 X -> DB 커넥션 점유 안 함)
        TossPaymentResponse tossResponse = tossPaymentsClient.confirm(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        try {
            // 2. [DB 처리] 결제 완료 처리
            return paymentService.processPaymentSuccess(request, tossResponse);
        } catch (Exception e) {
            // 3. [보상 트랜잭션] DB 저장 실패 시 이미 승인된 결제 취소
            log.error("결제 저장 실패로 인한 취소 처리. orderId: {}", request.orderId(), e);
            tossPaymentsClient.cancel(tossResponse.paymentKey(), "서버 내부 오류로 인한 자동 취소");
            throw e;
        }
    }
}