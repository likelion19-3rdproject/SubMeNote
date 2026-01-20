package com.backend.notification.service.impl;

import com.backend.global.exception.domain.NotificationErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.notification.dto.NotificationReadResponseDto;
import com.backend.notification.dto.NotificationResponseDto;
import com.backend.notification.entity.Notification;
import com.backend.notification.repository.NotificationRepository;
import com.backend.notification.service.NotificationService;
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

    private final NotificationRepository notificationRepository;


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
}
