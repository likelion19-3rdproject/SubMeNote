package com.backend.order.repository;

import com.backend.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.backend.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);

    @Modifying
    @Query("""
            delete from Order o
            where o.status = :status
            and o.expiredAt < :now
            """)
    int deleteExpiredOrders(@Param("status") OrderStatus status,
                            @Param("now") LocalDateTime now);

    @Query("""
            select o.id from Order o
            where o.status = :status 
            and o.expiredAt < :now
            """)
    List<Long> findExpiredOrders(@Param("status") OrderStatus status,
                                 @Param("now") LocalDateTime now);

    Page<Order> findByUserId(Long userId, Pageable pageable);
}
