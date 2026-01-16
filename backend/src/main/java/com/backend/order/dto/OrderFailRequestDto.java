package com.backend.order.dto;

public record OrderFailRequestDto(
        String orderId,
        String code,
        String message
) {
}