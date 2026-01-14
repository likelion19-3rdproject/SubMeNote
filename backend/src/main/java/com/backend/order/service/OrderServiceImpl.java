package com.backend.order.service;

import com.backend.global.exception.OrderErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.order.dto.OrderResponseDto;
import com.backend.order.entity.Order;
import com.backend.order.entity.OrderStatus;
import com.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    // 주문 전체 조회
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrderList(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(OrderResponseDto::from);
    }

    // 주문 상세 조회
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResponseDto.from(order);
    }

    // 주문 상태 수정
    @Override
    public OrderResponseDto update(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        OrderStatus orderStatus = OrderStatus.valueOf(status);

        // 상태 변경
        order.changeStatus(orderStatus);

        return OrderResponseDto.from(order);
    }
}
