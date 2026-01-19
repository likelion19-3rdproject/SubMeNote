package com.backend.payment.controller;

import com.backend.order.dto.OrderFailRequestDto;
import com.backend.payment.dto.PaymentConfirmRequest;
import com.backend.payment.dto.PaymentResponse;
import com.backend.payment.service.PaymentFacade; // Facade import
import com.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class TossPaymentController {

    private final PaymentFacade paymentFacade; // Service -> Facade로 변경
    private final PaymentService paymentService; // fail 처리를 위해 필요하다면 남겨둠

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirm(@RequestBody PaymentConfirmRequest request) {
        // Facade 호출
        return ResponseEntity.ok(paymentFacade.confirmPayment(request));
    }

    @PostMapping("/fail")
    public ResponseEntity<Map<String, String>> fail(@RequestBody OrderFailRequestDto request) {
        // fail 처리
        paymentService.failPayment(request.orderId(), request.code());

        return ResponseEntity.ok(Map.of(
                "code", request.code(),
                "message", request.message(),
                "orderId", request.orderId()
        ));
    }
}