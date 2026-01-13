package com.backend.payment.dto;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
) {
    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }
}