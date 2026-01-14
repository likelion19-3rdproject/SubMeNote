package com.backend.settlement.entity;

import com.backend.settlement_item.entity.SettlementItem;
import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Settlement <-> Creator N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // Settlement <-> SettlementItem 1:N
    @OneToMany(mappedBy = "settlement", fetch = FetchType.LAZY)
    private List<SettlementItem> settlementItems = new ArrayList<>();

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public static Settlement create(User creator, LocalDate periodStart, LocalDate periodEnd, Long totalAmount) {
        Settlement settlement = new Settlement();
        settlement.creator = creator;
        settlement.periodStart = periodStart;
        settlement.periodEnd = periodEnd;
        settlement.totalAmount = totalAmount;
        settlement.status = SettlementStatus.PENDING;
        return settlement;
    }

    // SettlementItem 추가
    public void addSettlementItem(SettlementItem item) {
        this.settlementItems.add(item);
        item.assignToSettlement(this);
        this.totalAmount += item.getSettlementAmount();
    }

    // 정산 확정
    public void complete() {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = LocalDateTime.now();
    }
}
