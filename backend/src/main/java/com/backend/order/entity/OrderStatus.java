package com.backend.order.entity;

public enum OrderStatus {
    PENDING, // 결제 요청
    IN_PROGRESS, // 결제 중
    CANCELED, // 사용자가 결제창 닫음
    PAID, // 결제 성공
    FAILED, // 결제 실패
    EXPIRED // 시간 초과
    ;
}
