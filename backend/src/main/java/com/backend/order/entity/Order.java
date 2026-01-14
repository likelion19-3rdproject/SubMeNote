package com.backend.order.entity;

import com.backend.payment.entity.Payment;
import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // Order <-> Payment 1:1
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createAt;

    public Order(User user, User creator, String orderId, String orderName, int amount , String method, OrderStatus status) {
        this.user = user;
        this.creator = creator;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    // 편의 메서드
    public void completePayment(Payment payment){
        this.payment = payment;
        this.status = OrderStatus.COMPLETED;
    }

    public void cancelPayment() {
        this.status = OrderStatus.CANCELED;
    }
}
