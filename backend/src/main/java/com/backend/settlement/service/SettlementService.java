package com.backend.settlement.service;

import com.backend.settlement.dto.SettlementResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementService {
    Page<SettlementResponseDto> getMySettlements(Long creatorId, Pageable pageable);
}
