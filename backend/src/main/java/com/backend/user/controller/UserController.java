package com.backend.user.controller;

import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.service.CommentService;
import com.backend.global.util.CookieUtil;
import com.backend.global.util.CustomUserDetails;
import com.backend.user.dto.*;
import com.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 일반 유저들이 할 수 있는 일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;
    private final CommentService commentService;
    private final CookieUtil cookieUtil;

    /**
     * 내 정보 조회
     */
    @GetMapping
    public ResponseEntity<UserResponseDto> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        UserResponseDto user = userService.getMe(userDetails.getUserId());

        return ResponseEntity.ok(user);
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<?> signout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {

        userService.signout(userDetails.getUserId());

        cookieUtil.clearTokens(response);

        return ResponseEntity.noContent().build();
    }

    /**
     * 내가 작성한 댓글 조회
     */
    @GetMapping("/comments")
    public ResponseEntity<Page<CommentResponseDto>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<CommentResponseDto> response
                = commentService.getMyComments(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 크리에이터 신청
     */
    @PostMapping("/creator-application")
    public ResponseEntity<?> applyForCreator(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        userService.applyForCreator(userDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    /**
     * 크리에이터 신청 내역 조회
     */
    @GetMapping("/creator-application")
    public ResponseEntity<CreatorApplicationResponseDto> getMyApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        CreatorApplicationResponseDto myApplication
                = userService.getMyApplication(userDetails.getUserId());

        return ResponseEntity.ok(myApplication);
    }
}