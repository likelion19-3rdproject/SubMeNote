package com.backend.settlement.service;

import com.backend.global.exception.SettlementErrorCode;
import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.role.entity.RoleEnum;
import com.backend.settlement.dto.SettlementDetailResponse;
import com.backend.settlement.dto.SettlementItemResponse;
import com.backend.settlement.dto.SettlementResponseDto;
import com.backend.settlement.entity.Settlement;
import com.backend.settlement.repository.SettlementRepository;
import com.backend.settlement_item.repository.SettlementItemRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final UserRepository userRepository;


    /**
     * 정산 내역 조회
     * <br/>
     * 1. CREATOR 권한 확인
     * 2. 정산 내역 조회
     */


    @Override
    public Page<SettlementResponseDto> getMySettlements(Long creatorId, Pageable pageable) {
        // CREATOR 권한 확인
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        // 정산 내역 조회
        Page<Settlement> settlements
                = settlementRepository.findByCreatorIdOrderByPeriodEndDesc(creatorId, pageable);

        return settlements.map(settlement
                        -> SettlementResponseDto.from(settlement, user.getNickname())
                );
    }

    @Transactional(readOnly = true)
    public SettlementDetailResponse getSettlementDetail(Long settlementId, Long loginUserId, Pageable pageable) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        // ✅ 권한 체크: 내 정산만 조회 가능
        if (!settlement.getCreator().getId().equals(loginUserId)) {
            throw new BusinessException(SettlementErrorCode.SETTLEMENT_FORBIDDEN);
        }

        Page<SettlementItemResponse> items = settlementItemRepository
                .findBySettlementId(settlementId, pageable)
                .map(i -> new SettlementItemResponse(
                        i.getId(),
                        i.getPayment().getId(),
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

    /**
     * 대기 중인 정산 조회 (settlement_id가 null인 SettlementItem)
     * <br/>
     * 1. CREATOR 권한 확인
     * 2. settlement_id가 null인 SettlementItem 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SettlementItemResponse> getPendingSettlementItems(Long creatorId, Pageable pageable) {
        // CREATOR 권한 확인
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        // settlement_id가 null인 SettlementItem 조회 (아직 Settlement에 포함되지 않은 정산 항목)
        Page<SettlementItemResponse> items = settlementItemRepository
                .findByCreatorIdAndSettlementIdIsNullOrderByCreatedAtDesc(creatorId, pageable)
                .map(item -> new SettlementItemResponse(
                        item.getId(),
                        item.getPayment().getId(),
                        item.getTotalAmount(),
                        item.getPlatformFee(),
                        item.getSettlementAmount(),
                        item.getStatus(),
                        item.getCreatedAt()
                ));

        return items;
    }

}
