package com.backend.order.service;

import com.backend.order.dto.OrderCreateRequestDto;
import com.backend.order.dto.OrderCreateResponseDto;

public interface OrderService {

    OrderCreateResponseDto createOrder(Long userId, Long creatorId);

}
