package com.backend.notification.service;

import com.backend.notification.dto.NotificationContext;
import com.backend.notification.dto.NotificationReadResponse;
import com.backend.notification.dto.NotificationResponseDto;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationService {
    Page<NotificationResponseDto> findMyNotifications(Long userId, Pageable pageable);

    void announceToAll(Long adminId, String message);

    NotificationResponseDto findNotification(Long userId, Long notificationId);

    void deleteNotification(Long userId, Long notificationId);
    NotificationReadResponse readNotifications(Long userId, List<Long> ids);
}
