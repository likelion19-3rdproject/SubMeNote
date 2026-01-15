package com.backend.order.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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

    @Column(name = "order_id",nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = true)
    private String method;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public Order(User user, User creator, String orderId, String orderName, Long amount, String method, OrderStatus status, LocalDateTime expiredAt) {
        this.user = user;
        this.creator = creator;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.expiredAt = expiredAt;
    }

    // 결제 성공
    public void complete() {
        this.status = OrderStatus.PAID;
    }
    //결제 취소(창닫기)
    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }
    //결제 실패
    public void fail() {
        this.status = OrderStatus.FAILED;
    }

    //상태 변경 메소드
    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}
