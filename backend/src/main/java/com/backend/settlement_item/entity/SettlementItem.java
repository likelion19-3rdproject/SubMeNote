package com.backend.settlement_item.entity;

import com.backend.payment.entity.Payment;
import com.backend.settlement.entity.Settlement;
import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_items")
@Getter
@NoArgsConstructor
public class SettlementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SettlementItem <-> Payment 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    // SettlementItem <-> Settlement N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    // SettlementItem <-> Creator N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name="total_amount", nullable=false)
    private Long totalAmount; // 결제 금액

    @Column(name="platform_fee", nullable=false)
    private Long platformFee; // 플랫폼 수수료 (10%)

    @Column(name="settlement_amount", nullable=false)
    private Long settlementAmount; // 정산 금액 (90%)

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private SettlementItemStatus status;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    private SettlementItem(Payment payment, User creator, Long totalAmount, Long platformFee, Long settlementAmount, SettlementItemStatus status, LocalDateTime createdAt) {
        this.payment = payment;
        this.creator = creator;
        this.totalAmount = totalAmount;
        this.platformFee = platformFee;
        this.settlementAmount = settlementAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static SettlementItem create(Payment payment) {
        return new SettlementItem(
                payment, payment.getCreator(),
                (long) payment.getAmount(),
                (long) (payment.getAmount() * 0.1),
                (long) (payment.getAmount() * 0.9),
                SettlementItemStatus.RECORDED,
                LocalDateTime.now()
        );
    }

//    public static SettlementItem recorded(Long creatorId, Long paymentId, Long totalAmount) {
//        SettlementItem settlementItem = new SettlementItem();
//        settlementItem.creatorId = creatorId;
//        settlementItem.paymentId = paymentId;
//        settlementItem.totalAmount = totalAmount;
//        settlementItem.platformFee = (long) (totalAmount * 0.1);
//        settlementItem.settlementAmount = (long) (totalAmount * 0.9);
//        settlementItem.status = SettlementItemStatus.RECORDED;
//        settlementItem.createdAt = LocalDateTime.now();
//        return settlementItem;
//    }

    public void assignToSettlement(Settlement settlement) {
        this.settlement = settlement;
        this.status = SettlementItemStatus.CONFIRMED;
    }

//    public void assignToSettlement(Long settlementId) {
//        this.settlementId = settlementId;
//    }

    public void confirm(){
        this.status = SettlementItemStatus.CONFIRMED;
    }
}
