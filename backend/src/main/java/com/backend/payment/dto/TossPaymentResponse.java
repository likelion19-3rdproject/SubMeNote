package com.backend.payment.dto;

import java.time.OffsetDateTime;

public record TossPaymentResponse(
        String paymentKey,
        int totalAmount,
        String method,
        OffsetDateTime approvedAt

) {
    public static TossPaymentResponse success(String paymentKey, int amount, String method,OffsetDateTime approvedAt){
        return new TossPaymentResponse(
                paymentKey,
                amount,
                method,
                approvedAt
        );
    }
}
