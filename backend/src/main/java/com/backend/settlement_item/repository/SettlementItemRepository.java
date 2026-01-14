package com.backend.settlement_item.repository;

import com.backend.settlement_item.entity.SettlementItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    // 정산 상세 조회용
    Page<SettlementItem> findBySettlementId(Long settlementId, Pageable pageable);



}
