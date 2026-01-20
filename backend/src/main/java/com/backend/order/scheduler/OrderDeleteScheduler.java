package com.backend.order.scheduler;

import com.backend.order.entity.OrderStatus;
import com.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderDeleteScheduler {

    private final OrderRepository orderRepository;

    @Transactional
    @Scheduled(fixedDelay = 3 * 60 * 1000)  //3분마다
    public void cleanupExpiredOrders() {

        LocalDateTime now = LocalDateTime.now();

        List<Long> expiredOrders = orderRepository.findExpiredOrders(OrderStatus.PENDING, now);

        int deleted = orderRepository.deleteExpiredOrders(OrderStatus.PENDING, now);

        expiredOrders.forEach(id -> log.info("삭제된 orderId: {}", id));

        log.info("삭제된 order 갯수 : {}", deleted);
    }
}