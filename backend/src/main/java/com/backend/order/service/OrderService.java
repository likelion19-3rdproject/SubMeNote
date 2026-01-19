package com.backend.order.service;

import com.backend.order.dto.OrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.backend.order.dto.OrderCreateResponseDto;

public interface OrderService {

    // 주문 전체 조회
    Page<OrderResponseDto> getOrderList(Long userId, Pageable pageable);

    // 주문 생성
    OrderCreateResponseDto createOrder(Long userId, Long creatorId, String orderName, Long amount);

    // 주문 상세 조회
    OrderResponseDto getOrder(Long userId, Long orderId);

    // 주문 상태 수정
    OrderResponseDto update(Long orderId, String status);
}
