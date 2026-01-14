package com.backend.order.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.order.dto.OrderCreateRequestDto;
import com.backend.order.dto.OrderCreateResponseDto;
import com.backend.order.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping("")
    public ResponseEntity<OrderCreateResponseDto> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrderCreateRequestDto requestDto
            ){
        OrderCreateResponseDto order = orderService.createOrder(userDetails.getUserId(),requestDto.creatorId());

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

}
