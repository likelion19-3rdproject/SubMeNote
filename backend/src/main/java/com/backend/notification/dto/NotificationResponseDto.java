package com.backend.notification.dto;

import com.backend.notification.entity.Notification;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.TargetType;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        NotificationType notificationType,
        TargetType targetType,
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
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
