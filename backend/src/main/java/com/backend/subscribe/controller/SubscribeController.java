package com.backend.subscribe.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.subscribe.dto.SubscribedCreatorResponseDto;
import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.dto.SubscribeStatusUpdateRequestDto;
import com.backend.subscribe.service.SubscribeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscribes")
public class SubscribeController {

    private final SubscribeService subscribeService;

    // 구독하기
    @PostMapping("/{creatorId}")
    public ResponseEntity<SubscribeResponseDto> createSubscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long creatorId) {

        SubscribeResponseDto responseDto = subscribeService.createSubscribe(
                userDetails.getUserId(),
                creatorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 구독 상태 변경 (구독/취소)
    @PatchMapping("/{subscribeId}")
    public ResponseEntity<SubscribeResponseDto> updateSubscribeStatus(
            //추후 security 구현되면 변경
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long subscribeId,
            @Valid @RequestBody SubscribeStatusUpdateRequestDto requestDto) {

        SubscribeResponseDto responseDto = subscribeService.updateStatus(
                userDetails.getUserId(),
                subscribeId,
                requestDto.status());

        return ResponseEntity.ok(responseDto);
    }

    // 구독 정보 삭제
    @DeleteMapping("/{subscribeId}")
    public ResponseEntity<Void> deleteSubscribe(
            //추후 security 구현되면 변경
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long subscribeId) {

        subscribeService.deleteSubscribe(
                userDetails.getUserId(),
                subscribeId);

        return ResponseEntity.noContent().build();
    }

    // 구독 목록
    @GetMapping("/my-creator")
    public ResponseEntity<Page<SubscribedCreatorResponseDto>> findSubscribeCreator(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int page, @RequestParam int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("type"),
                        Sort.Order.desc("createdAt")
                ));

        Page<SubscribedCreatorResponseDto> responseDto
                = subscribeService.findSubscribedCreator(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/membership/renew")
    public ResponseEntity<Void> renewMembership(
            @RequestParam Long creatorId,
            @RequestParam int months,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        subscribeService.renewMembership(userDetails.getUserId(), creatorId, months);
        return ResponseEntity.noContent().build();
    }

}