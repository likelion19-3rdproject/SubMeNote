package com.backend.settlement.batch;


import com.backend.role.entity.RoleEnum;
import com.backend.settlement.service.SettlementBatchServiceImpl;
import com.backend.settlement_item.service.SettlementItemBatchServiceImpl;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final UserRepository userRepository;
    private final SettlementItemBatchServiceImpl settlementItemBatchService;
    private final SettlementBatchServiceImpl settlementBatchService;

    /**
     * 매주 월요일 00:10 - 지난주(월~일) 원장 기록
     */
    //@Scheduled(cron = "0 10 0 * * MON")
    @Scheduled(cron = "0 */1 * * * *")
    public void recordWeeklyLedger() {



        List<User> creators = userRepository.findAllByRoleEnum(RoleEnum.ROLE_CREATOR);

        for (User creator : creators) {
            try {
                //이번주 원장기록
                int createdThisWeek = settlementItemBatchService.recordThisWeekLedger(creator.getId());
                log.info("thisWeek ledger recorded. creatorId={}, created={}", creator.getId(), createdThisWeek);

                int created = settlementItemBatchService.recordLastWeekLedger(creator.getId());
                log.info("weekly ledger recorded. creatorId={}, created={}", creator.getId(), created);
            } catch (Exception e) {
                log.error("weekly ledger record failed. creatorId={}", creator.getId(), e);
            }
        }
    }

    /**
     * 매월 1일 00:20 - 전월 정산 확정
     */
    //@Scheduled(cron = "0 20 0 1 * *")
    @Scheduled(cron = "0 */1 * * * *")
    public void confirmMonthlySettlement() {

        //  UserRepository에 List<User> findAllByRoleEnum(RoleEnum roleEnum) 필요
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
// 스케줄러에서는 인터페이스에 정의된 메서드만 호출해야 하므로,
// 기준일(today)은 서비스 내부에서 계산하도록 분리