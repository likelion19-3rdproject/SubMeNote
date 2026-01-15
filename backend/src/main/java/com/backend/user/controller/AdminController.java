package com.backend.user.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.user.dto.ApplicationProcessRequestDto;
import com.backend.user.dto.CreatorApplicationResponseDto;
import com.backend.user.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    // 대기 중인 크리에이터 신청 목록
    @GetMapping("/creator-applications")
    public ResponseEntity<Page<CreatorApplicationResponseDto>> getPendingApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<CreatorApplicationResponseDto> applications
                = adminService.getPendingApplications(userDetails.getUserId(), page, size);

        return ResponseEntity.ok(applications);
    }

    // 크리에이터 승인/거절
    @PatchMapping("/creator-applications/{applicationId}")
    public ResponseEntity<?> approveOrRejectApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationProcessRequestDto requestDto
            ) {

        adminService.processApplication(userDetails.getUserId(), applicationId, requestDto);

        return ResponseEntity.ok().build();
    }
}
