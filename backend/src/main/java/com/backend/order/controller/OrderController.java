package com.backend.order.controller;

import com.backend.order.dto.OrderResponseDto;
import com.backend.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
