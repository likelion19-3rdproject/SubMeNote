package com.backend.notification.service;

import com.backend.global.exception.domain.NotificationErrorCode;
import com.backend.global.exception.domain.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.notification.dto.AnnouncementResponseDto;
import com.backend.notification.dto.NotificationContext;
import com.backend.notification.dto.NotificationReadResponseDto;
import com.backend.notification.dto.NotificationResponseDto;
import com.backend.notification.entity.Notification;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.NotificationTargetType;
import com.backend.notification.repository.NotificationRepository;
import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationCommand notificationCommand;

    /**
     * 내 알림 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> findMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponseDto::from);
    }

    /**
     * 알림 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDto findNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(NotificationErrorCode.FORBIDDEN);
        }

        return NotificationResponseDto.from(notification);
    }

    /**
     * 알림 삭제
     */
    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(NotificationErrorCode.FORBIDDEN);
        }

        notificationRepository.delete(notification);
    }

    /**
     * 알림 읽음 처리
     */
    @Override
    @Transactional
    public NotificationReadResponseDto readNotifications(Long userId, List<Long> ids) {

        int updated = notificationRepository
                .UpdateReadByUserAndIds(userId, ids, LocalDateTime.now());

        return NotificationReadResponseDto.from(ids.size(), updated);
    }

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

        User admin = userRepository.findByIdOrThrow(userId);

        if (!admin.hasRole(RoleEnum.ROLE_ADMIN)) {
            throw new BusinessException(UserErrorCode.ADMIN_ONLY);
        }

        Page<Notification> announcements = notificationRepository
                .findDistinctAnnouncements(
                        NotificationType.ANNOUNCEMENT,
                        pageable
                );

        return announcements.map(AnnouncementResponseDto::from);
    }
}
