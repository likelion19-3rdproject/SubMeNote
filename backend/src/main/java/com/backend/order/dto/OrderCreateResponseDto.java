package com.backend.order.dto;

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
