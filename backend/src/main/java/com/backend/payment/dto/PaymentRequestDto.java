package com.backend.payment.dto;

public record PaymentRequestDto(
        String orderId,
        String orderName,
        int amount,
        String customerName,
        String successUrl,
        String failUrl
) {
}
