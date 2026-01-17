package com.backend.notification.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.notification.dto.AnnouncementRequest;
import com.backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}