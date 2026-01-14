package com.backend.payment.dto;

import java.time.OffsetDateTime;

public record TossPaymentResponse(
        String paymentKey,
        Long totalAmount,
        String method,
        OffsetDateTime approvedAt

) {
    public static TossPaymentResponse success(String paymentKey, Long amount, String method,OffsetDateTime approvedAt){
        return new TossPaymentResponse(
                paymentKey,
                amount,
                method,
                approvedAt
        );
    }
}
