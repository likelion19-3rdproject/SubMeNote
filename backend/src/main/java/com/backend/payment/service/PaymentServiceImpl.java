package com.backend.payment.service;

import com.backend.global.exception.domain.OrderErrorCode;
import com.backend.global.exception.domain.PaymentErrorCode;
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
import com.backend.subscribe.service.SubscribeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscribeService subscriptionService;


    /**
     * 결제 완료 처리
     * <br/>
     * 1. 주문 조회
     * 2. 주문 검증
     * 3. 결제 정보 저장
     * 4. 주문 상태 완료 처리
     * 5. 구독 서비스 활성화
     */
    @Override
    @Transactional
    public PaymentResponse processPaymentSuccess(PaymentConfirmRequest request, TossPaymentResponse tossResponse) {

        // 1. 주문 조회
        Order order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. 검증 (이미 결제된 주문인지, 금액이 맞는지)
        validateOrder(order, request.amount());

        // 3. 결제 정보 저장
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

        // 4. 주문 상태 완료 처리
        order.complete(tossResponse.method());

        // 5. 구독 서비스 활성화
        subscriptionService.renewMembership(
                order.getUser().getId(),
                order.getCreator().getId(),
                1
        );

        return PaymentResponse.from(payment);
    }

    private void validateOrder(Order order, Long requestAmount) {

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException(PaymentErrorCode.ALREADY_PAID);
        }

        if (!order.getAmount().equals(requestAmount)) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    /**
     * 결제 실패
     */
    @Override
    @Transactional
    public void failPayment(String orderId, String failCode) {

        // 주문 찾기
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        //상태 변경 분기 처리
        if ("PAY_PROCESS_CANCELED".equals(failCode) || "USER_CANCEL".equals(failCode)) {
            //사용자가 x버튼 눌러서 닫은 경우
            order.cancel();
        } else {
            //잔액 부족, 카드사 거절, 네트워크 에러 등
            order.fail();
        }

        orderRepository.save(order);
    }
}