package com.backend.subscribe.dto;

import com.backend.subscribe.entity.SubscribeStatus;

public record SubscribeStatusUpdateRequestDto(

        SubscribeStatus status
) {
}