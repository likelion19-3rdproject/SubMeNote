package com.backend.settlement_item.repository;

import com.backend.settlement_item.entity.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
}
