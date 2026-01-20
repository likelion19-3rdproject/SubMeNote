package com.backend.order.dto;

import com.backend.order.entity.Order;
import com.backend.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponseDto(
        Long id,
        String orderId,
        String orderName,
        Long amount,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderId(),
                order.getOrderName(),
                order.getAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
