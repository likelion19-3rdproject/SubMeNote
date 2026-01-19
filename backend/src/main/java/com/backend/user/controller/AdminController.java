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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 대기 중인 크리에이터 신청 목록
     */
    @GetMapping("/creator-applications")
    public ResponseEntity<Page<CreatorApplicationResponseDto>> getPendingApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(
                    sort = "appliedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<CreatorApplicationResponseDto> applications
                = adminService.getPendingApplications(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(applications);
    }

    /**
     * 크리에이터 승인/거절
     */
    @PatchMapping("/creator-applications/{applicationId}")
    public ResponseEntity<?> approveOrRejectApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationProcessRequestDto requestDto
    ) {

        adminService.processApplication(
                userDetails.getUserId(),
                applicationId,
                requestDto
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 크리에이터 수 조회
     */
    @GetMapping("/creators/count")
    public ResponseEntity<Long> getCreatorCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long creatorCount
                = adminService.getCreatorCount(userDetails.getUserId());

        return ResponseEntity.ok(creatorCount);
    }

    /**
     * 크리에이터 목록 조회
     */
    @GetMapping("/creators/list")
    public ResponseEntity<Page<CreatorResponseDto>> getCreatorList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault Pageable pageable
    ) {

        Page<CreatorResponseDto> creatorList
                = adminService.getCreatorList(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(creatorList);
    }

    /**
     * 회원 수 조회
     */
    @GetMapping("/users/count")
    public ResponseEntity<Long> getMemberCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userCount
                = adminService.getUserCount(userDetails.getUserId());

        return ResponseEntity.ok(userCount);
    }

    /**
     * 회원 목록 조회
     */
    @GetMapping("/users/list")
    public ResponseEntity<Page<UserResponseDto>> getMemberList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault Pageable pageable
    ) {

        Page<UserResponseDto> userList
                = adminService.getUserList(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(userList);
    }
}
