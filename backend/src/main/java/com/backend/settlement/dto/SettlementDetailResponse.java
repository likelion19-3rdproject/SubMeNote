package com.backend.settlement.dto;

import com.backend.settlement.entity.SettlementStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 정산 상세 조회 API의 전체 응답
public record SettlementDetailResponse(
        Long settlementId,
        LocalDate periodStart,
        LocalDate periodEnd,
        Long totalAmount,
        SettlementStatus status,
        LocalDateTime settledAt,
        Page<SettlementItemResponse> items
) {
    public static SettlementDetailResponse from(Long settlementId,
                                                LocalDate periodStart,
                                                LocalDate periodEnd,
                                                Long totalAmount,
                                                SettlementStatus status,
                                                LocalDateTime settledAt,
                                                Page<SettlementItemResponse> items){
        return SettlementDetailResponse.from(
                settlementId,
                periodStart,
                periodEnd,
                totalAmount,
                status,
                settledAt,
                items
        );
    }

}
