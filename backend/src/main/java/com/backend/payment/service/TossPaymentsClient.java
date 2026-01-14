package com.backend.payment.service;


import com.backend.payment.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    public TossPaymentsClient() {
        this.restTemplate = new RestTemplate();
        this.secretKey = secretKey;
    }

    public TossPaymentResponse confirm(String paymentKey, String orderId, int amount) {
        // 테스트 모드: 더미 키를 사용하는 경우 Mock 응답 반환
        if (secretKey == null || secretKey.equals("test_sk_dummy") || secretKey.isEmpty()) {
            log.warn("테스트 모드: 토스페이먼츠 API를 호출하지 않고 Mock 응답을 반환합니다.");
            return new TossPaymentResponse(
                    paymentKey,
                    amount,
                    "CARD"
            );
        }

        try {
            // Basic 인증 헤더
            String credentials = secretKey + ":";
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedCredentials);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 바디
            Map<String, Object> body = Map.of(
                    "paymentKey", paymentKey,
                    "orderId", orderId,
                    "amount", amount
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // API 호출
            ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    request,
                    TossPaymentResponse.class
            );
            log.info("토스페이먼츠 API 응답: {}", response.getBody());

            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("토스페이먼츠 API 호출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("토스페이먼츠 결제 승인 실패: " + e.getMessage());
        }
    }
}