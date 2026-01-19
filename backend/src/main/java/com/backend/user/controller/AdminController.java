package com.backend.user.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.user.dto.ApplicationProcessRequestDto;
import com.backend.user.dto.CreatorApplicationResponseDto;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.UserResponseDto;
import com.backend.user.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "appliedAt")
        );

        Page<CreatorApplicationResponseDto> applications
                = adminService.getPendingApplications(userDetails.getUserId(), pageable);

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

    // 크리에이터 수 조회
    @GetMapping("/creators/count")
    public ResponseEntity<Long> getCreatorCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        Long creatorCount = adminService.getCreatorCount(userId);

        return ResponseEntity.ok(creatorCount);
    }

    // 크리에이터 목록 조회
    @GetMapping("/creators/list")
    public ResponseEntity<Page<CreatorResponseDto>> getCreatorList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Long userId = userDetails.getUserId();

        Page<CreatorResponseDto> creatorList
                = adminService.getCreatorList(userId, pageable);

        return ResponseEntity.ok(creatorList);
    }

    // 회원 수 조회
    @GetMapping("/users/count")
    public ResponseEntity<Long> getMemberCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        Long userCount = adminService.getUserCount(userId);

        return ResponseEntity.ok(userCount);
    }

    // 회원 목록 조회
    @GetMapping("/users/list")
    public ResponseEntity<Page<UserResponseDto>> getMemberList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Long userId = customUserDetails.getUserId();

        Page<UserResponseDto> userList
                = adminService.getUserList(userId, pageable);

        return ResponseEntity.ok(userList);
    }
}
