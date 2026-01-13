package com.backend.settlement_item.entity;

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

    @Column(name="creator_id",nullable = false)
    private Long creatorId;

    @Column(name="payment_id", nullable=false)
    private Long paymentId;

    @Column(name="total_amount", nullable=false)
    private Long totalAmount;

    @Column(name="platform_fee", nullable=false)
    private Long platformFee;

    @Column(name="settlement_amount", nullable=false)
    private Long settlementAmount;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private SettlementItemStatus status;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    public static SettlementItem recorded(Long creatorId, Long paymentId, Long totalAmount, Long platformFee, Long settlementAmount) {
        SettlementItem settlementItem = new SettlementItem();
        settlementItem.creatorId = creatorId;
        settlementItem.paymentId = paymentId;
        settlementItem.totalAmount = totalAmount;
        settlementItem.platformFee = platformFee;
        settlementItem.settlementAmount =totalAmount - platformFee ;
        settlementItem.status = SettlementItemStatus.RECORDED;
        settlementItem.createdAt = LocalDateTime.now();
        return settlementItem;
    }

    public void confirm(){
        this.status = SettlementItemStatus.CONFIRMED;
    }
}
