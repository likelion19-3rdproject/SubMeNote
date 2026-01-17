package com.backend.notification.dto;

import com.backend.notification.entity.Notification;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.NotificationTargetType;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        NotificationType notificationType,
        NotificationTargetType notificationTargetType,
        Long targetId,
        String title,
        String message,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getNotificationType(),
                notification.getNotificationTargetType(),
                notification.getTargetId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
