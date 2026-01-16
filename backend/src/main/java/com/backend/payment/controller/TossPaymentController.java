package com.backend.payment.controller;

import com.backend.order.dto.OrderFailRequestDto; // DTO import 필요 (없으면 새로 생성하거나 Map 사용)
import com.backend.payment.dto.PaymentConfirmRequest;
import com.backend.payment.dto.PaymentResponse;
import com.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments") // [1] 공통 URL Prefix 설정
public class TossPaymentController {

    private final PaymentService paymentService;

    /**
     * [결제 승인 API]
     * URL: POST /api/payments/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirm(@RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }

    /**
     * [결제 실패 처리 API]
     * 프론트엔드(React)의 실패 페이지에서 호출
     * URL: POST /api/payments/fail
     */
    @PostMapping("/fail")
    public ResponseEntity<Map<String, String>> fail(@RequestBody OrderFailRequestDto request) {
        // 1. 서비스 호출 (DB 상태 업데이트: PENDING -> FAILED)
        paymentService.failPayment(request.orderId(), request.code());

        // 2. 응답 반환
        return ResponseEntity.ok(Map.of(
                "code", request.code(),
                "message", request.message(),
                "orderId", request.orderId()
        ));
    }
}