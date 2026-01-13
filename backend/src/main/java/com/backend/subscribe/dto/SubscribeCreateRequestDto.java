package com.backend.subscribe.dto;

import com.backend.subscribe.entity.SubscribeType;
import jakarta.validation.constraints.NotNull;

public record SubscribeCreateRequestDto(
        @NotNull(message = "")
        SubscribeType type
) {}