package com.backend.payment.dto;

public record PaymentRequestResponseDto(
        String paymentKey,
        String orderId,
        int amount,
        String orderName,
        String checkoutUrl
) {
}
