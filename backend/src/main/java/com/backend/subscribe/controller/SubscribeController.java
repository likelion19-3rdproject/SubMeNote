package com.backend.subscribe.controller;

import com.backend.subscribe.dto.SubscribeCreateRequestDto;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscribes")
public class SubscribeController {

    private final SubscribeService subscribeService;

    @PostMapping("/{creatorId}")
    public ResponseEntity<SubscribeResponseDto> createSubscribe(
            //추후 security 구현되면 변경
            @RequestParam Long userId,
            @PathVariable Long creatorId,
            @RequestBody SubscribeCreateRequestDto requestDto) {
        SubscribeResponseDto responseDto = subscribeService.createSubscribe(userId, creatorId ,requestDto.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PatchMapping("/{subscribeId}")
    public ResponseEntity<SubscribeResponseDto> updateSubscribeStatus(
            //추후 security 구현되면 변경
            @RequestParam Long userId,
            @PathVariable Long subscribeId,
            @Valid @RequestBody SubscribeStatusUpdateRequestDto requestDto) {
        SubscribeResponseDto responseDto = subscribeService.updateStatus(userId, subscribeId, requestDto.status());
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{subscribeId}")
    public ResponseEntity<Void> deleteSubscribe(
            //추후 security 구현되면 변경
            @RequestParam Long userId,
            @PathVariable Long subscribeId) {
        subscribeService.deleteSubscribe(userId, subscribeId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/my-creator")
    public ResponseEntity<Page<SubscribedCreatorResponseDto>> findSubscribeCreator(
            @RequestParam Long userId,
            @RequestParam int page, @RequestParam int size){
        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Order.desc("type"),Sort.Order.desc("createdAt")));
        Page<SubscribedCreatorResponseDto> responseDto = subscribeService.findSubscribedCreator(userId, pageable);
        return ResponseEntity.ok(responseDto);
    }


}