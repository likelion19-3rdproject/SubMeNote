package com.backend.payment.service;

import com.backend.payment.dto.PaymentConfirmRequest;
import com.backend.payment.dto.PaymentResponse;
import com.backend.payment.dto.TossPaymentResponse;

public interface PaymentService {

    //결제 승인 요청 (토스 API 호출 및 DB 저장)
    PaymentResponse processPaymentSuccess(PaymentConfirmRequest request, TossPaymentResponse tossResponse);

    //결제 실패/취소 처리
    void failPayment(String orderId, String failCode);
}