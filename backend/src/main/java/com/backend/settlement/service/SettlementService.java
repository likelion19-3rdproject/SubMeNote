package com.backend.settlement.service;

import com.backend.global.exception.SettlementErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.settlement.dto.SettlementDetailResponse;
import com.backend.settlement.dto.SettlementItemResponse;
import com.backend.settlement.entity.Settlement;


import com.backend.settlement.repository.SettlementRepository;
import com.backend.settlement_item.repository.SettlementItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    @Transactional(readOnly = true)
    public SettlementDetailResponse getSettlementDetail(Long settlementId, Long loginUserId, Pageable pageable) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        // ✅ 권한 체크: 내 정산만 조회 가능
        if (!settlement.getCreatorId().equals(loginUserId)) {
            throw new BusinessException(SettlementErrorCode.SETTLEMENT_FORBIDDEN);
        }

        Page<SettlementItemResponse> items = settlementItemRepository
                .findBySettlementId(settlementId, pageable)
                .map(i -> new SettlementItemResponse(
                        i.getId(),
                        i.getPaymentId(),
                        i.getTotalAmount(),
                        i.getPlatformFee(),
                        i.getSettlementAmount(),
                        i.getStatus(),
                        i.getCreatedAt()
                ));

        return new SettlementDetailResponse(
                settlement.getId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                settlement.getTotalAmount(),
                settlement.getStatus(),
                settlement.getSettledAt(),
                items
        );
    }
}
