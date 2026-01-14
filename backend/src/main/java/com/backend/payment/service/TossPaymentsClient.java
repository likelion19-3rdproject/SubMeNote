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
        // 1. 테스트 모드 체크 (Mock 응답)
        if (secretKey == null || secretKey.equals("test_sk_dummy") || secretKey.isEmpty()) {
            log.warn("테스트 모드: Mock 응답 반환");
            return TossPaymentResponse.success(paymentKey, amount, "CARD", OffsetDateTime.now());
        }

        // 2. 인코딩
        String encodedAuth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 3. API 호출
        return restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", "Basic " + encodedAuth)
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