package com.backend.notification.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.notification.dto.AnnouncementRequestDto;
import com.backend.notification.dto.AnnouncementResponseDto;
import com.backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationService notificationService;

    /**
     * 전체 공지
     */
    @PostMapping("/announcement")
    public void announceToAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AnnouncementRequestDto request
    ) {
        notificationService.announceToAll(userDetails.getUserId(), request.message());
    }

    /**
     * 공지 목록 보기
     */
    @GetMapping("/announcement")
    public ResponseEntity<Page<AnnouncementResponseDto>> getAllAnnouncements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {

        Long userId = userDetails.getUserId();

        Page<AnnouncementResponseDto> announcementList
                = notificationService.getAnnouncementList(userId, pageable);

        return ResponseEntity.ok(announcementList);
    }
}