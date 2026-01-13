package com.backend.subscribe.dto;

import com.backend.subscribe.entity.SubscribeStatus;
import jakarta.validation.constraints.NotNull;

public record SubscribeStatusUpdateRequestDto(
        @NotNull(message = "")
        SubscribeStatus status
) {}