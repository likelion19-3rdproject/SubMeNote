package com.backend.settlement.dto;

import com.backend.settlement.entity.Settlement;
import com.backend.settlement.entity.SettlementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementResponseDto(
        Long id,
        Long creatorId,
        String creatorNickname,  // 크리에이터 닉네임
        LocalDate periodStart,
        LocalDate periodEnd,
        Long totalAmount,
        SettlementStatus status,
        LocalDateTime settledAt
) {

    public static SettlementResponseDto from(Settlement settlement, String creatorNickname) {
        return new SettlementResponseDto(
                settlement.getId(),
                settlement.getCreatorId(),
                creatorNickname,
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                settlement.getTotalAmount(),
                settlement.getStatus(),
                settlement.getSettledAt()
        );
    }
}
