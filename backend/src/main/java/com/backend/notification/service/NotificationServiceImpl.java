package com.backend.notification.service;

import com.backend.global.exception.NotificationErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.notification.dto.NotificationContext;
import com.backend.notification.dto.NotificationReadResponse;
import com.backend.notification.dto.NotificationResponseDto;
import com.backend.notification.entity.Notification;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.TargetType;
import com.backend.notification.repository.NotificationRepository;
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


    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> findMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponseDto::from);
    }

    @Override
    @Transactional
    public void announceToAll(Long adminId, String message) {
        userRepository.findAll().forEach(u ->
                notificationCommand.createNotification(
                        u.getId(),
                        NotificationType.ANNOUNCEMENT,
                        TargetType.NONE,
                        null,
                        NotificationContext.forAnnouncement(message)
                )
        );
    }

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
    @Override
    @Transactional
    public NotificationReadResponse readNotifications(Long userId, List<Long> ids) {
        int updated = notificationRepository.UpdateReadByUserAndIds(userId, ids, LocalDateTime.now());
        return new NotificationReadResponse(ids.size(), updated);
    }

}
