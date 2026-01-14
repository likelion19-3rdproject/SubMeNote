package com.backend.payment.entity;

import com.backend.order.entity.Order;
import com.backend.settlement.entity.Settlement;
import com.backend.settlement_item.entity.SettlementItem;
import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Payment <-> Order 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Payment <-> SettlementItem 1:1
    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private SettlementItem settlementItem;

    @Column(name = "payment_key", nullable = false, unique = true)
    private String paymentKey; // 토스 결제 고유 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    public static Payment create(Order order, String paymentKey, LocalDate paidAt) {
        Payment payment = new Payment();
        payment.order = order;
        payment.user = order.getUser();
        payment.creator = order.getCreator();
        payment.amount = order.getAmount();
        payment.paymentKey = paymentKey;
        payment.status = PaymentStatus.DONE;
        payment.paidAt = paidAt;

        // 양방향 관계 설정
        order.completePayment(payment);

        return payment;
    }

    // 정산 원장 생성
    public SettlementItem createSettlementItem() {
        SettlementItem item = SettlementItem.create(this);
        this.settlementItem = item;
        return item;
    }

    public static Payment create(User user, User creator, int amount, String paymentKey, LocalDate paidAt) {
        Payment payment = new Payment();
        payment.user = user;
        payment.creator = creator;
        payment.amount = amount;
        payment.paymentKey = paymentKey;
        payment.status = PaymentStatus.DONE;
        payment.paidAt = paidAt;

        return payment;
    }
}
