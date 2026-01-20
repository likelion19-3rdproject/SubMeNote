package com.backend.subscribe.scheduler;

import com.backend.notification.dto.NotificationContext;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.NotificationTargetType;
import com.backend.notification.service.NotificationCommand;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscribeScheduler {

    private final SubscribeRepository subscribeRepository;
    private final NotificationCommand notificationCommand;

    // 매일 9시마다 체크 (만료 7일 전/3일 전 알림)
    @Transactional(readOnly = true)
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyExpiringSubscriptions() {
        LocalDate today = LocalDate.now();

        LocalDate target7 = today.plusDays(7);
        LocalDate target3 = today.plusDays(3);

        List<Subscribe> expiringIn7Days =
                subscribeRepository.findExpiringAt(SubscribeStatus.ACTIVE, target7);

        List<Subscribe> expiringIn3Days =
                subscribeRepository.findExpiringAt(SubscribeStatus.ACTIVE, target3);

        log.info("[SubscribeScheduler] Expire notification start");
        for (Subscribe s : expiringIn7Days) {
            notificationCommand.createNotification(
                    s.getUser().getId(),
                    NotificationType.SUBSCRIBE_EXPIRE_SOON,
                    NotificationTargetType.SUBSCRIBE,
                    s.getId(),
                    NotificationContext.forExpire(
                            s.getCreator().getNickname(),
                            7
                    )
            );
        }

        for (Subscribe s : expiringIn3Days) {
            notificationCommand.createNotification(
                    s.getUser().getId(),
                    NotificationType.SUBSCRIBE_EXPIRE_SOON,
                    NotificationTargetType.SUBSCRIBE,
                    s.getId(),
                    NotificationContext.forExpire(
                            s.getCreator().getNickname(),
                            3
                    )
            );
        }

        log.info("[SubscribeScheduler] Expire notification finished. 7days={}, 3days={}",
                expiringIn7Days.size(),
                expiringIn3Days.size()
        );
    }

    // 매일 0시 0분 10초에 체크 (만료된 멤버쉽 구독 ->일반구독으로 전환)
    @Transactional
    @Scheduled(cron = "10 0 0 * * *")
    public void expirePaidSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscribe> expired = subscribeRepository.findExpiredBefore(SubscribeStatus.ACTIVE, today);

        for (Subscribe s : expired) {
                s.renewFree();
            log.info("[toFREE] - subscribeId={}, userId={}, creatorId={}, expiredAt={}",
                    s.getId(),
                    s.getUser().getId(),
                    s.getCreator().getId(),
                    s.getExpiredAt());
        }
    }
}