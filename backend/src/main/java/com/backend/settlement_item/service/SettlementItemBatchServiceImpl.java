package com.backend.settlement_item.service;

import com.backend.global.exception.domain.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.payment.entity.Payment;
import com.backend.payment.repository.PaymentRepository;
import com.backend.role.entity.RoleEnum;
import com.backend.settlement.util.SettlementPeriod;
import com.backend.settlement_item.entity.SettlementItem;
import com.backend.settlement_item.repository.SettlementItemRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SettlementItemBatchServiceImpl implements SettlementItemBatchService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementItemRepository settlementItemRepository;

    /**
     * 지난주 원장 기록
     */
    @Override
    @Transactional
    public int recordLastWeekLedger(Long creatorId) {

        User creator = userRepository.findByIdOrThrow(creatorId);

        if (!creator.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        LocalDate today = LocalDate.now();
        SettlementPeriod.Range range = SettlementPeriod.lastWeekMonToSun(today);

        // Payment는 paidAt(LocalDate) 기준 조회
        List<Payment> payments = paymentRepository.findByCreator_IdAndPaidAtBetween(
                creatorId,
                range.start(),
                range.end().toLocalDate().atTime(LocalTime.MAX)
        );

        return createSettlementItemsBatch(payments);
    }

    @Override
    @Transactional
    public int syncLedgerUpToNow(Long creatorId) {

        // 1. 유저 검증
        if (!userRepository.existsById(creatorId)) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        // 2. 조회 범위 설정 (이번 달 1일 ~ 현재)
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = YearMonth.now().atDay(1).atStartOfDay();

        List<Payment> payments = paymentRepository.findByCreator_IdAndPaidAtBetween(
                creatorId,
                start,
                end
        );

        return createSettlementItemsBatch(payments);
    }

    // ✅ 중복 로직 추출 및 최적화 (Bulk Insert)
    private int createSettlementItemsBatch(List<Payment> payments) {

        if (payments.isEmpty()) {
            return 0;
        }

        // 이미 원장에 기록된 Payment ID 조회 (Bulk 조회)
        List<Long> paymentIds = payments.stream().map(Payment::getId).toList();

        // paymentIds가 있는데 Set 조회가 비어있을 수 있으므로 null safe 처리 필요할 수 있으나 JPA는 보통 빈 Set 반환
        Set<Long> recordedPaymentIds = settlementItemRepository.findPaymentIdsByPaymentIdsIn(paymentIds);

        List<SettlementItem> newItems = new ArrayList<>();

        for (Payment p : payments) {
            // 이미 기록된 건은 패스
            if (recordedPaymentIds.contains(p.getId())) continue;
            // 결제 실패/취소 건 등은 create 내부 혹은 여기서 필터링 필요
            if (p.getPaidAt() == null) continue;

            newItems.add(SettlementItem.create(p));
        }

        if (!newItems.isEmpty()) {
            settlementItemRepository.saveAll(newItems);
        }

        return newItems.size();
    }

    /**
     * 이번주 원장 기록
     */
    @Override
    @Transactional
    public int recordThisWeekLedger(Long creatorId) {

        // 1. 유저 조회
        if (!userRepository.existsById(creatorId)) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        // 2. 기간 설정: 이번 주 월요일 ~ 현재 (혹은 이번주 일요일)
        LocalDate today = LocalDate.now();
        SettlementPeriod.Range range = SettlementPeriod.thisWeekMonToSun(today); // 유틸 클래스 활용

        // 3. Payment 조회
        List<Payment> payments = paymentRepository.findByCreator_IdAndPaidAtBetween(
                creatorId,
                range.start(), // 이번주 월요일 00:00:00
                range.end()    // 이번주 일요일 23:59:59
        );

        // 4. 공통 메서드로 저장 위임 (중복 체크 및 Bulk Insert)
        return createSettlementItemsBatch(payments);
    }
}