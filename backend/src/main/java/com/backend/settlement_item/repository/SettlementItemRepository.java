package com.backend.settlement_item.repository;

import com.backend.settlement_item.entity.SettlementItem;
import com.backend.settlement_item.entity.SettlementItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    // 정산 상세 조회용
    Page<SettlementItem> findBySettlementId(Long settlementId, Pageable pageable);
    //
    boolean existsByPaymentId(Long paymentId);

    // 월 정산 확정 대상
    List<SettlementItem> findByCreatorIdAndStatusAndSettlementIdIsNullAndCreatedAtBetween(
            Long creatorId,
            SettlementItemStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    // 대기 중인 정산 조회 (settlement_id가 null인 SettlementItem)
    Page<SettlementItem> findByCreatorIdAndSettlementIdIsNullOrderByCreatedAtDesc(
            Long creatorId,
            Pageable pageable
    );

}
