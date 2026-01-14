package com.backend.order.controller;

import com.backend.order.dto.OrderResponseDto;
import com.backend.order.service.OrderService;
import com.backend.global.util.CustomUserDetails;
import com.backend.order.dto.OrderCreateRequestDto;
import com.backend.order.dto.OrderCreateResponseDto;
import com.backend.order.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * 주문 전체 조회
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrderList(
            @RequestParam Long userId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<OrderResponseDto> orders = orderService.getOrderList(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("")
    public ResponseEntity<OrderCreateResponseDto> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrderCreateRequestDto requestDto
            ) {
        OrderCreateResponseDto order = orderService.createOrder(userDetails.getUserId(), requestDto.creatorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @PathVariable Long orderId
    ) {
        OrderResponseDto order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * 주문 상태 수정
     */
    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long orderId,
            @RequestParam String status
    ) {
        OrderResponseDto updatedOrder = orderService.update(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }
}
