package com.backend.payment.dto;

import com.backend.payment.entity.Payment;
import com.backend.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        String orderId,
        String paymentKey,
        Long amount,
        PaymentStatus status,
        LocalDateTime paidAt
) {
    public static PaymentResponse from(Payment payment){
        return new PaymentResponse(
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}
