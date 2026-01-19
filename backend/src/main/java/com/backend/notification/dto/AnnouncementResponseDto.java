package com.backend.notification.dto;

import com.backend.notification.entity.Notification;

import java.time.LocalDateTime;

public record AnnouncementResponseDto(
        String message,
        LocalDateTime createdAt
) {
    public static AnnouncementResponseDto from(Notification notification) {
        return new AnnouncementResponseDto(
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
