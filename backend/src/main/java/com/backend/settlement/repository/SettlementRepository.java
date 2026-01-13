package com.backend.settlement.repository;

import com.backend.settlement.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Page<Settlement> findByCreatorIdOrderByPeriodEndDesc(Long creatorId, Pageable pageable);
}
