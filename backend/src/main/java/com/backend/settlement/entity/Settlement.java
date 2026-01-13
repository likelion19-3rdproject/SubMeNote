package com.backend.settlement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

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

    public static Settlement create(Long creatorId, LocalDate periodStart, LocalDate periodEnd, Long totalAmount) {
        Settlement settlement = new Settlement();
        settlement.creatorId = creatorId;
        settlement.periodStart = periodStart;
        settlement.periodEnd = periodEnd;
        settlement.totalAmount = totalAmount;
        settlement.settledAt = null;
        settlement.status = SettlementStatus.COMPLETED;
        return settlement;
    }
}
