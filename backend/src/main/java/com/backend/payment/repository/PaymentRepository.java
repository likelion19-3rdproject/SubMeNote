package com.backend.payment.repository;

import com.backend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 * 배치 전용 조회라 list 사용
 *
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCreator_IdAndPaidAtBetween(
            Long creatorId,
            // settlement변경
            LocalDateTime start,
            LocalDateTime end
    );

}
