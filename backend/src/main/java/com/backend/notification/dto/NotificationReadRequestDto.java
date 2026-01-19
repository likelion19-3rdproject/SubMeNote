package com.backend.notification.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotificationReadRequestDto(

        @NotNull
        List<Long> notificationIds
) {
}
