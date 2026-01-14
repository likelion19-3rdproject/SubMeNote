package com.backend.settlement.service;

public interface SettlementBatchService {
    /**
     * 전월(1일~말일) 기준으로 정산 확정 처리
     */
    void confirmLastMonth(Long creatorId);
}
