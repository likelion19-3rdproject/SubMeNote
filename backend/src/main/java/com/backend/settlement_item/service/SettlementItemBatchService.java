package com.backend.settlement_item.service;

public interface SettlementItemBatchService {
    /**
     * 지난주(월~일) 결제건을 정산 원장(SettlementItem)으로 기록한다.
     * @return 생성된 원장 개수
     */
    int recordLastWeekLedger(Long creatorId);


}
