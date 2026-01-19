package com.backend.settlement.service;

import com.backend.settlement.dto.SettlementDetailResponse;
import com.backend.settlement.dto.SettlementResponseDto;
import com.backend.settlement.dto.SettlementItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface SettlementService {
    Page<SettlementResponseDto> getMySettlements(Long creatorId, Pageable pageable);

    SettlementDetailResponse getSettlementDetail(Long settlementId, Long loginUserId, Pageable pageable);

    // 대기 중인 정산 조회 (settlement_id가 null인 SettlementItem)
    Page<SettlementItemResponse> getPendingSettlementItems(Long creatorId, Pageable pageable);

}