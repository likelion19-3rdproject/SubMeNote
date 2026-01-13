package com.backend.payment.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    public Payment(User user, User creator, int amount, String paymentKey,PaymentStatus status, LocalDate paidAt) {
        this.user = user;
        this.creator = creator;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
        this.paidAt = paidAt;
    }

    public static Payment create(User user, User creator, int amount, String paymentKey, LocalDate paidAt) {
        return new Payment(
                user,
                creator,
                amount,
                paymentKey
                , PaymentStatus.DONE,
                paidAt
        );
    }
}
