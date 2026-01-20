package com.backend.payment.service.impl;

import com.backend.global.exception.domain.PaymentErrorCode;
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
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    private final RestClient restClient;

    //결제 승인 요청 (Toss API)
    public TossPaymentResponse confirm(String paymentKey, String orderId, Long amount) {
        String encodedAuth = getEncodedAuth();

        return restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", encodedAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "paymentKey", paymentKey,
                        "orderId", orderId,
                        "amount", amount
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    // 4xx 에러: 잘못된 요청 (이미 승인됨, 잔액 부족 등)
                    log.error("Toss 결제 승인 4xx 에러 - orderId: {}", orderId);
                    throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_KEY);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    // 5xx 에러: 토스 서버 장애
                    log.error("Toss 결제 승인 5xx 에러 - orderId: {}", orderId);
                    throw new BusinessException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
                })
                .body(TossPaymentResponse.class);
    }

    /**
     * 결제 취소 요청
     * DB 저장 실패 시, 이미 승인된 결제를 롤백하기 위함
     */
    public void cancel(String paymentKey, String cancelReason) {
        String encodedAuth = getEncodedAuth();

        try {
            restClient.post()
                    .uri("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel")
                    .header("Authorization", encodedAuth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("cancelReason", cancelReason))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        // 취소 API 호출 자체가 실패했을 때의 처리
                        throw new BusinessException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
                    })
                    .toBodilessEntity(); // 응답 바디가 필요 없을 때 사용

            log.info("결제 취소 성공 - paymentKey: {}, reason: {}", paymentKey, cancelReason);

        } catch (Exception e) {
            //취소조차 실패한 경우 (Double Fault)
            log.error("🚨 긴급: 결제 취소 실패! 수동 환불 필요. paymentKey: {}, error: {}", paymentKey, e.getMessage());
        }
    }

    /**
     * Basic Auth 헤더 생성 (SecretKey 인코딩)
     */
    private String getEncodedAuth() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }
}