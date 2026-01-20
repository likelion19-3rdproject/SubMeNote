package com.backend.settlement.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.settlement.dto.SettlementResponseDto;
import com.backend.settlement.dto.SettlementDetailResponse;
import com.backend.settlement.dto.SettlementItemResponse;
import com.backend.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 정산 조회
     */
    @GetMapping
    public ResponseEntity<Page<SettlementResponseDto>> getMySettlements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(
                    sort = "periodEnd",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<SettlementResponseDto> responseDto
                = settlementService.getMySettlements(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 정산 세부내역 조회
     */
    @GetMapping("/{settlementId}")
    public SettlementDetailResponse getSettlementDetail(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault Pageable pageable
    ) {
        return settlementService.getSettlementDetail(settlementId, user.getUserId(), pageable);
    }

    /**
     * 대기 중인 정산 조회 (settlement_id가 null인 SettlementItem)
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<SettlementItemResponse>> getPendingSettlementItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<SettlementItemResponse> responseDto
                = settlementService.getPendingSettlementItems(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 즉시 정산 처리
     */
    @PostMapping("/immediate")
    public ResponseEntity<SettlementResponseDto> settleImmediately(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        SettlementResponseDto response = settlementService.settleImmediately(userDetails.getUserId());

        return ResponseEntity.ok(response);
    }
}
