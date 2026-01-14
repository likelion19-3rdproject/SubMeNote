package com.backend.order.dto;

import com.backend.order.entity.Order;
import com.backend.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderCreateResponseDto(
        String orderId,
        String orderName,
        long amount
) {
    public static OrderCreateResponseDto success(String orderId, String orderName, long amount){
        return new OrderCreateResponseDto(
                orderId,
                orderName,
                amount
        );
    }
}
