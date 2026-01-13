package com.backend.payment.service;

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
import com.backend.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscribeService subscriptionService;
    private final TossPaymentsClient tossPaymentsClient;

    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {

        Order order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        //금액 검증
        if (order.getAmount() != request.getAmount()) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다");
        }

        //중복 결제 방지
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalArgumentException("이미 결제된 주문입니다");
        }


        // 4️⃣ 토스페이먼츠 결제 승인 API 호출
        TossPaymentResponse tossResponse = tossPaymentsClient.confirm(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        // 5️⃣ 결제 정보 저장
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .paymentKey(tossResponse.paymentKey())
                .amount(tossResponse.totalAmount())
                .user(new User())
                .creator(new User())
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        /*// 6️⃣ 주문 상태 변경
        order.complete();

        // 7️⃣ 구독 활성화
        subscriptionService.activate(order.getUserId(), order.getCourseId());*/

        return PaymentResponse.success(payment);
    }
}