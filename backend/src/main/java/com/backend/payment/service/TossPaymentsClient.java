package com.backend.payment.service;

import com.backend.global.exception.PaymentErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.payment.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    @Value("${toss.payments.secret-key}")
    private String secretKey;
    
    private final RestClient restClient;

    public TossPaymentResponse confirm(String paymentKey, String orderId, Long amount) {
        // 1. 테스트용 결제 실패 시뮬레이션 (orderId에 특정 패턴이 포함된 경우)
        // if (orderId != null && orderId.contains("_REJECT_CARD_COMPANY")) {
        //     log.warn("테스트 모드: 카드사 거절 시뮬레이션 - orderId: {}", orderId);
        //     throw new BusinessException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        // }

//        // 2. 테스트 모드 체크 (Mock 응답)
//        if (secretKey == null || secretKey.equals("test_sk_dummy") || secretKey.isEmpty()) {
//            log.warn("테스트 모드: Mock 응답 반환");
//            return TossPaymentResponse.success(paymentKey, amount, "CARD", OffsetDateTime.now());
//        }

        // 3. 인코딩
        String encodedAuth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 4. API 호출
        return restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", "Basic " + encodedAuth)
                // 주석을 해제하면 설정한 에러가 강제로 발생
                // 1. 잔액 부족 / 한도 초과 등 카드사 거절
//                 .header("TossPayments-Test-Code", "REJECT_CARD_PAYMENT")
                // 2. 금액 불일치 에러
                // .header("TossPayments-Test-Code", "PAYMENT_400_1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "paymentKey", paymentKey,
                        "orderId", orderId,
                        "amount", amount
                ))
                .retrieve()
                // 4xx, 5xx 에러 처리
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_KEY);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new BusinessException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
                })
                .body(TossPaymentResponse.class);
    }
}