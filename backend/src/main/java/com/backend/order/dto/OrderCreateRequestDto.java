package com.backend.order.dto;

public record OrderCreateRequestDto(
        Long creatorId,
        String orderName,
        int amount
) {
}
