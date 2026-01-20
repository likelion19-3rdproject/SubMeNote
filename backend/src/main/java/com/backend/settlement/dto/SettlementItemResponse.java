package com.backend.settlement.dto;

import com.backend.settlement_item.entity.SettlementItemStatus;

import java.time.LocalDateTime;

// 그 응답 안에 포함된 정산 원장 한 줄
public record SettlementItemResponse(
        Long id,
        Long paymentId,
        Long totalAmount,
        Long platformFee,
        Long settlementAmount,
        SettlementItemStatus status,
        LocalDateTime createdAt
) {
}
