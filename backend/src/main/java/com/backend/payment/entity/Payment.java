package com.backend.payment.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Payment(String orderId, String paymentKey, User user, User creator, Long amount, PaymentStatus status, LocalDateTime paidAt) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.user = user;
        this.creator = creator;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
    }

    public static Payment of(String orderId, String paymentKey, User user, User creator, Long amount, PaymentStatus status, LocalDateTime paidAt){
        return new Payment(orderId,paymentKey,user, creator, amount, status, paidAt);
    }

    @Column(name = "paid_at")
    private LocalDateTime paidAt; //결제 승인 일시(pg사와 카드사가 인정한 실제 결제 시간, 토스 응답approvedAt 값)

    @CreatedDate
    private LocalDateTime createdAt; // 실제 DB 저장 시간

    @LastModifiedDate
    private LocalDateTime updatedAt; //추후 환불, 취소 시 사용
}
