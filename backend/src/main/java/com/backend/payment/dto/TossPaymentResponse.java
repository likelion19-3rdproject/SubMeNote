package com.backend.payment.dto;

public record TossPaymentResponse(
        String paymentKey,
        int totalAmount,
        String Method

) {
    public Object getPaymentKey() {
        return paymentKey;
    }
}
