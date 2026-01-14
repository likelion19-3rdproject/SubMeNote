package com.backend.settlement.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.settlement.dto.SettlementDetailResponse;
import com.backend.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/{settlementId}")
    public SettlementDetailResponse getSettlementDetail(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable
    ) {
        return settlementService.getSettlementDetail(settlementId, user.getUserId(), pageable);
    }
}