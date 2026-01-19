package com.backend.settlement_item.service;

import com.backend.global.exception.UserErrorCode;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementItemBatchServiceImpl implements  SettlementItemBatchService {
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementItemRepository settlementItemRepository;

    /**
     * 지난주(월~일) 결제건을 SettlementItem(RECORDED)로 기록
     * return 생성된 settlementItem 수
     */
    @Override
    @Transactional
    public int recordThisWeekLedger(Long creatorId) {
        LocalDate today = LocalDate.now();

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!creator.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        // ✅ 이번주 월요일~일요일 범위
        LocalDate thisWeekMon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate thisWeekSun = thisWeekMon.plusDays(6);

        LocalDateTime start = thisWeekMon.atStartOfDay();
        LocalDateTime end = thisWeekSun.atTime(LocalTime.MAX);

        log.info("[BATCH-DEV] thisWeek start={}, end={}", start, end);

        List<Payment> payments = paymentRepository.findByCreator_IdAndPaidAtBetween(
                creatorId, start, end
        );

        int created = 0;
        for (Payment p : payments) {
            if (p.getPaidAt() == null) continue;
            if (settlementItemRepository.existsByPaymentId(p.getId())) continue;

            settlementItemRepository.save(SettlementItem.create(p));
            created++;
        }

        log.info("[BATCH-DEV] thisWeek ledger created={}", created);
        return created;
    }

    @Override
    @Transactional
    public int recordLastWeekLedger(Long creatorId) {
        LocalDate today = LocalDate.now();

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!creator.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        SettlementPeriod.Range range = SettlementPeriod.lastWeekMonToSun(today);

        // settlement 관련 수정
        LocalDateTime start = range.start();
        LocalDateTime end = range.end().toLocalDate().atTime(LocalTime.MAX);



        // Payment는 paidAt(LocalDate) 기준 조회
        List<Payment> payments = paymentRepository.findByCreator_IdAndPaidAtBetween(
                creatorId,
                start,
                end
        );

        int created = 0;
        for (Payment p : payments) {
            if (p.getPaidAt() == null) continue;
            if (settlementItemRepository.existsByPaymentId(p.getId())) continue;

            settlementItemRepository.save(SettlementItem.create(p));
            created++;
        }

        return created;
    }

}
