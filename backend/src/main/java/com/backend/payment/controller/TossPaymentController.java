package com.backend.payment.controller;

import com.backend.payment.dto.PaymentConfirmRequest;
import com.backend.payment.dto.PaymentResponse;
import com.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TossPaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirm(
            @RequestBody PaymentConfirmRequest request) {

        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/confirm")
    public ResponseEntity<PaymentResponse> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount) {
        PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, orderId, amount);

        PaymentResponse response = paymentService.confirmPayment(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/fail")
    public String fail(){

        return "fail";
    }
}


