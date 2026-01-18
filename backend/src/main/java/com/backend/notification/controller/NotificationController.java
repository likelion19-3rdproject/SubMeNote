package com.backend.notification.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.notification.dto.NotificationReadRequest;
import com.backend.notification.dto.NotificationReadResponse;
import com.backend.notification.dto.NotificationResponseDto;
import com.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 목록
    @GetMapping
    public Page<NotificationResponseDto> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationService.findMyNotifications(userDetails.getUserId(), pageable);
    }

    // 알림 상세조회
    @GetMapping("/{notificationId}")
    public NotificationResponseDto getMyNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        return notificationService.findNotification(userDetails.getUserId(), notificationId);
    }

    //읽음 처리
    @PatchMapping("/read")
    public NotificationReadResponse readMany(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationReadRequest request
    ) {
        return notificationService.readNotifications(userDetails.getUserId(), request.notificationIds());
    }
    //삭제
    @DeleteMapping("/{notificationId}")
    public void deleteMyNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        notificationService.deleteNotification(userDetails.getUserId(), notificationId);
    }
}
