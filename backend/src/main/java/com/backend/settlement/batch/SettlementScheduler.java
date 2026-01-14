package com.backend.settlement.batch;


import com.backend.role.entity.RoleEnum;
import com.backend.settlement.service.SettlementBatchService;
import com.backend.settlement_item.service.SettlementItemBatchService;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final UserRepository userRepository;
    private final SettlementItemBatchService settlementItemBatchService;
    private final SettlementBatchService settlementBatchService;

    /**
     * 매주 월요일 00:10 - 지난주(월~일) 원장 기록
     */
    @Scheduled(cron = "0 10 0 * * MON")
    public void recordWeeklyLedger() {
        LocalDate today = LocalDate.now();

        // ⚠️ UserRepository에 List<User> findAllByRoleEnum(RoleEnum roleEnum) 필요
        List<User> creators = userRepository.findAllByRoleEnum(RoleEnum.ROLE_CREATOR);

        for (User creator : creators) {
            try {
                int created = settlementItemBatchService.recordLastWeekLedger(creator.getId(), today);
                log.info("weekly ledger recorded. creatorId={}, created={}", creator.getId(), created);
            } catch (Exception e) {
                log.error("weekly ledger record failed. creatorId={}", creator.getId(), e);
            }
        }
    }

    /**
     * 매월 1일 00:20 - 전월 정산 확정
     */
    @Scheduled(cron = "0 20 0 1 * *")
    public void confirmMonthlySettlement() {

        // ⚠️ UserRepository에 List<User> findAllByRoleEnum(RoleEnum roleEnum) 필요
        List<User> creators = userRepository.findAllByRoleEnum(RoleEnum.ROLE_CREATOR);

        for (User creator : creators) {
            try {
                settlementBatchService.confirmLastMonth(creator.getId());
                log.info("monthly settlement confirmed. creatorId={}", creator.getId());
            } catch (Exception e) {
                log.error("monthly settlement confirm failed. creatorId={}", creator.getId(), e);
            }
        }
    }
}
