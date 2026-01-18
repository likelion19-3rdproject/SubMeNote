package com.backend.notification.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotificationReadRequest(
        @NotNull
        List<Long> notificationIds) {
}
