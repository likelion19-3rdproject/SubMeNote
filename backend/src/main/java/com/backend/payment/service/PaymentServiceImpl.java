package com.backend.payment.service;

import com.backend.global.exception.OrderErrorCode;
import com.backend.global.exception.PaymentErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.order.entity.Order;
import com.backend.order.entity.OrderStatus;
import com.backend.order.repository.OrderRepository;
import com.backend.payment.dto.PaymentConfirmRequest;
import com.backend.payment.dto.PaymentResponse;
import com.backend.payment.dto.TossPaymentResponse;
import com.backend.payment.entity.Payment;
import com.backend.payment.entity.PaymentStatus;
import com.backend.payment.repository.PaymentRepository;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.service.SubscribeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscribeService subscriptionService;
    private final TossPaymentsClient tossPaymentsClient;

    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        // 1. 주문 검증
        Order order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. 상태 및 금액 검증 (private 메서드 호출)
        validateOrder(order, request.amount());

        // 3. 토스 API 호출
        TossPaymentResponse tossResponse = tossPaymentsClient.confirm(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        // 4. 결제 정보 저장
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .paymentKey(tossResponse.paymentKey())
                .amount(tossResponse.totalAmount())
                .user(order.getUser())
                .creator(order.getCreator())
                .status(PaymentStatus.PAID)
                .paidAt(tossResponse.approvedAt().toLocalDateTime())
                .build();

        paymentRepository.save(payment);

        // 5. 주문 상태 완료 처리
        order.complete();

        // 6. 구독 서비스 활성화
        subscriptionService.createSubscribe(
                order.getUser().getId(),
                order.getCreator().getId(),
                SubscribeType.PAID
        );

        return PaymentResponse.from(payment);
    }

    /**
     * 주문 유효성 검증 (금액 및 상태)
     */
    private void validateOrder(Order order, Long requestAmount) {
        // 1. 이미 결제된 주문인지 확인
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException(PaymentErrorCode.ALREADY_PAID);
        }

        // 2. 금액이 일치하는지 확인
        if (!order.getAmount().equals(requestAmount)) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    public void failPayment(String orderId, String failCode) {
        // 1. 주문 찾기
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. 상태 변경 분기 처리
        if ("PAY_PROCESS_CANCELED".equals(failCode) || "USER_CANCEL".equals(failCode)) {
            // 사용자가 X 버튼 눌러서 닫은 경우
            order.cancel();
        } else {
            // 잔액 부족, 카드사 거절, 네트워크 에러 등
            order.fail();
        }

        // 3. 저장 (Dirty Checking으로 자동 저장되지만 명시적으로 save해도 됨)
        orderRepository.save(order);
    }
}