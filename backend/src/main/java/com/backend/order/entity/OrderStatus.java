package com.backend.order.entity;

public enum OrderStatus {
    PENDING, // 결제 요청
    IN_PROGRESS, // 결제 중
    PAID, // 결제 성공
    CANCELED, // 사용자 측에서 결제창 닫음
    FAILED, // 결제 취소
    EXPIRED // 시간 초과
}
