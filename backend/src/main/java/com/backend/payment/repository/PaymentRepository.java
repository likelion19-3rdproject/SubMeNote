package com.backend.payment.repository;

import com.backend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCreator_IdAndPaidAtBetween(Long creatorId, LocalDate start, LocalDate end);

}
