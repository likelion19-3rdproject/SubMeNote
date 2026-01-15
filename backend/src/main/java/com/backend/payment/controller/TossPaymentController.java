package com.backend.payment.controller;

import com.backend.payment.dto.PaymentConfirmRequest;
import com.backend.payment.dto.PaymentResponse;
import com.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TossPaymentController {
    private final PaymentService paymentService;

    /**
     * [API 연동용] 프론트엔드(React 등)에서 결제 승인 요청을 보낼 때 사용
     * URL: POST /api/payments/confirm
     */
    @PostMapping("/api/payments/confirm")
    public ResponseEntity<PaymentResponse> confirm(
            @RequestBody PaymentConfirmRequest request) {

        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * [테스트/리다이렉트용] 토스 결제창에서 성공 후 자동으로 이동하는 URL
     * URL: GET /confirm
     */
    @GetMapping("/confirm")
    public ResponseEntity<PaymentResponse> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount
    ) {
        // DTO 생성
        PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, orderId, amount);

        // 서비스 호출
        PaymentResponse response = paymentService.confirmPayment(request);

        return ResponseEntity.ok(response);
    }

    /**
     * [결제 실패 리다이렉트]
     * 파라미터로 실패 사유(code, message)
     */
    @GetMapping("/fail")
    public ResponseEntity<Map<String, String>> fail(
            @RequestParam String code, //토스가 준 코드
            @RequestParam String message, // 토스가 준 메시지
            @RequestParam String orderId
    ) {
        // 1.
        paymentService.failPayment(orderId, code);

        // 2. 에러 내용을 JSON으로 반환 (프론트엔드나 사용자가 볼 수 있게)
        return ResponseEntity.status(400).body(Map.of(
                "code", code,
                "message", message,
                "orderId", orderId
        ));
    }
}