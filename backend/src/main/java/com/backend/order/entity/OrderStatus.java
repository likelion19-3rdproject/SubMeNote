package com.backend.order.entity;

public enum OrderStatus {
    PENDING, // 결제 요청
    IN_PROGRESS, // 결제 중
    COMPLETED, // 결제 성공
    CANCELED,
    EXPIRED // 시간 초과
    ;
}
