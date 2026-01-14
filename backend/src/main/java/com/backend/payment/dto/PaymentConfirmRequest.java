package com.backend.payment.dto;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}