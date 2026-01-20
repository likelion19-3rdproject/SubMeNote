package com.backend.notification.dto;

public record NotificationReadResponseDto(
        int requested,
        int updated
) {
    public static NotificationReadResponseDto from (int requested, int updated){
        return new NotificationReadResponseDto(
                requested,
                updated
        );
    }
}
