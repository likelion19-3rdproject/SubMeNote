package com.backend.notification.dto;

public record NotificationReadResponse(
        int requested,
        int updated
) {
    public static NotificationReadResponse from (int requested, int updated){
        return new NotificationReadResponse(
                requested,
                updated
        );
    }
}
