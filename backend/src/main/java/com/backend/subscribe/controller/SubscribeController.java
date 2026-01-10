package com.backend.subscribe.controller;

import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.dto.SubscribeStatusUpdateRequestDto;
import com.backend.subscribe.service.SubscribeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscribes")
public class SubscribeController {

    private final SubscribeService subscribeService;

    @PostMapping("/{creatorId}")
    public ResponseEntity<SubscribeResponseDto> create(
            //추후 security 구현되면 변경
            @RequestParam Long userId,
            @PathVariable Long creatorId
    ) {
        SubscribeResponseDto responseDto = subscribeService.createSubscribe(userId, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PatchMapping("/{subscribeId}")
    public ResponseEntity<SubscribeResponseDto> updateStatus(
            //추후 security 구현되면 변경
            @RequestParam Long userId,
            @PathVariable Long subscribeId,
            @Valid @RequestBody SubscribeStatusUpdateRequestDto requestDto
    ) {
        SubscribeResponseDto responseDto = subscribeService.updateStatus(userId, subscribeId, requestDto.status());
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{subscribeId}")
    public ResponseEntity<Void> delete(
            //추후 security 구현되면 변경
            @RequestParam Long userId,
            @PathVariable Long subscribeId
    ) {
        subscribeService.deleteSubscribe(userId, subscribeId);
        return ResponseEntity.noContent().build();
    }
}