package com.backend.order.service.impl;

import com.backend.global.exception.domain.OrderErrorCode;
import com.backend.global.exception.domain.SubscribeErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.validator.RoleValidator;
import com.backend.order.dto.OrderResponseDto;
import com.backend.order.dto.OrderCreateResponseDto;
import com.backend.order.entity.Order;
import com.backend.order.entity.OrderStatus;
import com.backend.order.repository.OrderRepository;
import com.backend.order.service.OrderService;
import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RoleValidator roleValidator;

    /**
     * 주문 생성
     */
    @Override
    @Transactional
    public OrderCreateResponseDto createOrder(Long userId, Long creatorId, String orderName, Long amount) {

        User user = userRepository.findByIdOrThrow(userId);


        // 크리에이터인지 확인
        User creator = roleValidator.validateCreator(creatorId);

        // 자신이 자신을 구독하려고 하는지 체크
        if (userId.equals(creatorId)) {
            throw new BusinessException(SubscribeErrorCode.CANNOT_SUBSCRIBE_SELF);
        }

        String orderId = "order_" + UUID.randomUUID();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(30);

        Order order = Order.of(
                user,
                creator,
                orderId,
                orderName,
                amount,
                null,
                OrderStatus.PENDING,
                expiredAt
        );

        orderRepository.save(order);

        OrderCreateResponseDto orderResponse = OrderCreateResponseDto.success(orderId, orderName, amount);

        return orderResponse;
    }

    /**
     * 주문 전체 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrderList(Long userId, Pageable pageable) {

        return orderRepository
                .findByUserId(userId, pageable)
                .map(OrderResponseDto::from);
    }

    /**
     * 주문 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long userId, Long orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderResponseDto.from(order);
    }

    /**
     * 주문 상태 수정
     */
    @Override
    @Transactional
    public OrderResponseDto update(Long orderId, String status) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        OrderStatus orderStatus = OrderStatus.valueOf(status);

        // 상태 변경
        order.changeStatus(orderStatus);

        return OrderResponseDto.from(order);
    }
}
