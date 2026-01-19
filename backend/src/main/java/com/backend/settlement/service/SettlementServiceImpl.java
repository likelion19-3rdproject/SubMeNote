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
import com.backend.settlement_item.entity.SettlementItem;
import com.backend.settlement_item.entity.SettlementItemStatus;
import com.backend.settlement_item.repository.SettlementItemRepository;
import com.backend.settlement_item.service.SettlementItemBatchService;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor

public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final UserRepository userRepository;
    private final SettlementItemBatchService settlementItemBatchService;


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

    @Override
    @Transactional
    public SettlementResponseDto settleImmediately(Long creatorId) {
        // 1. 원장 최신화 (최근 결제 내역을 먼저 원장으로 변환)
        settlementItemBatchService.syncLedgerUpToNow(creatorId);

        // 2. 정산 대상 조회
        // 조건: 해당 크리에이터 + 상태 RECORDED + 아직 정산 안 됨(Settlement Null)
        List<SettlementItem> pendingItems = settlementItemRepository
                .findByCreatorIdAndStatusAndSettlementIdIsNull(
                        creatorId,
                        SettlementItemStatus.RECORDED
                );

        if (pendingItems.isEmpty()) {
            throw new BusinessException(SettlementErrorCode.NO_PENDING_SETTLEMENT); // 예외 처리 (정산할 게 없음)
        }

        // 3. 정산 기간 계산 (아이템들 중 가장 빠른 날짜 ~ 가장 늦은 날짜)
        // 혹은 "이번 달 1일 ~ 오늘"로 고정해도 되나, 실제 아이템 기준이 정확함
        LocalDate periodStart = pendingItems.stream()
                .map(item -> item.getPayment().getPaidAt().toLocalDate()) // Payment 시간 기준
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate periodEnd = LocalDate.now();

        // 4. Settlement 생성
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Settlement settlement = Settlement.create(creator, periodStart, periodEnd);
        settlementRepository.save(settlement);

        // 5. 아이템 연결 및 상태 변경
        for (SettlementItem item : pendingItems) {
            settlement.addSettlementItem(item); // 금액 누적 포함
            item.confirm();
        }

        settlement.complete(); // 최종 확정

        return SettlementResponseDto.from(settlement, creator.getNickname());
    }
}
