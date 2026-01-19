package com.backend.notification.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.notification.dto.AnnouncementRequest;
import com.backend.notification.dto.AnnouncementResponseDto;
import com.backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationService notificationService;


    @PostMapping("/announcement")
    public void announceToAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        notificationService.announceToAll(userDetails.getUserId(), request.message());
    }

    @GetMapping("/announcement")
    public ResponseEntity<Page<AnnouncementResponseDto>> getAllAnnouncements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Long userId = userDetails.getUserId();

        Page<AnnouncementResponseDto> announcementList
                = notificationService.getAnnouncementList(userId, pageable);

        return ResponseEntity.ok(announcementList);
    }
}