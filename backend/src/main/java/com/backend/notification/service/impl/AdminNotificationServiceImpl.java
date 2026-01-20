package com.backend.notification.service.impl;

import com.backend.global.validator.RoleValidator;
import com.backend.notification.dto.AnnouncementResponseDto;
import com.backend.notification.dto.NotificationContext;
import com.backend.notification.entity.Notification;
import com.backend.notification.entity.NotificationTargetType;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.repository.NotificationRepository;
import com.backend.notification.service.AdminNotificationService;
import com.backend.notification.service.NotificationCommand;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationCommand notificationCommand;
    private final RoleValidator roleValidator;

    /**
     * 전체 공지
     */
    @Override
    @Transactional
    public void announceToAll(Long adminId, String message) {

        userRepository
                .findAll()
                .forEach(u ->
                        notificationCommand.createNotification(
                                u.getId(),
                                NotificationType.ANNOUNCEMENT,
                                NotificationTargetType.NONE,
                                null,
                                NotificationContext.forAnnouncement(message)
                        )
                );
    }

    /**
     * 공지 목록 보기
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementResponseDto> getAnnouncementList(Long userId, Pageable pageable) {

        roleValidator.validateAdmin(userId);

        Page<Notification> announcements = notificationRepository
                .findDistinctAnnouncements(
                        NotificationType.ANNOUNCEMENT,
                        pageable
                );

        return announcements.map(AnnouncementResponseDto::from);
    }

}
