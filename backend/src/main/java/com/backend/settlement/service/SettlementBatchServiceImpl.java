package com.backend.settlement.service;

import com.backend.global.exception.domain.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.role.entity.RoleEnum;
import com.backend.settlement.entity.Settlement;
import com.backend.settlement.repository.SettlementRepository;

import com.backend.settlement.util.SettlementPeriod;
import com.backend.settlement_item.entity.SettlementItem;
import com.backend.settlement_item.entity.SettlementItemStatus;
import com.backend.settlement_item.repository.SettlementItemRepository;
import com.backend.user.entity.User;

import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementBatchServiceImpl implements SettlementBatchService {

    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    /**
     * 전월(1일~말일) 기준으로 정산 확정 처리
     * - RECORDED 이고 settlement이 없는 원장만 대상으로 한다.
     */
    @Override
    @Transactional
    public void confirmLastMonth(Long creatorId) {

        User creator = userRepository.findByIdOrThrow(creatorId);

        if (!creator.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        LocalDate today = LocalDate.now();
        SettlementPeriod.Range range = SettlementPeriod.lastMonth(today);

        List<SettlementItem> items = settlementItemRepository
                .findByCreatorIdAndStatusAndSettlementIdIsNullAndCreatedAtBetween(
                        creatorId,
                        SettlementItemStatus.RECORDED,
                        range.start(),
                        range.end()
                );

        if (items.isEmpty()) {
            return;
        }

        // Settlement는 0으로 시작해서 addSettlementItem에서 누적하는 게 안전
        Settlement settlement = Settlement.create(
                creator,
                range.start().toLocalDate(),
                range.end().toLocalDate(),
                0L
        );
        settlementRepository.save(settlement);

        for (SettlementItem item : items) {
            settlement.addSettlementItem(item); // settlement_id 세팅 + totalAmount 누적
            item.confirm();                     // CONFIRMED
        }

        settlement.complete(); // COMPLETED + settledAt
    }
}
