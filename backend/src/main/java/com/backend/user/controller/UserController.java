package com.backend.user.controller;

import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.service.CommentService;
import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.util.CustomUserDetails;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.service.PostService;
import com.backend.user.dto.CreatorAccountRequestDto;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.UserResponseDto;
import com.backend.user.entity.User;
import com.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    // 전체 크리에이터 조회
    @GetMapping("/home")
    public ResponseEntity<Page<CreatorResponseDto>> home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CreatorResponseDto> creators = userService.listAllCreators(page, size);
        return ResponseEntity.ok(creators);
    }

    // 내 정보 조회
    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDto> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserResponseDto user = userService.getMe(userId);
        return ResponseEntity.ok(user);
    }

    // 회원 탈퇴
    @DeleteMapping("/users/me")
    public ResponseEntity<?> signout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        Long userId = userDetails.getUserId();
        userService.signout(userId);

        // 쿠키 삭제
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .secure(false)
                .sameSite("Lax")
                .build();

        // refreshToken 쿠키 삭제
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(0)
                .secure(false)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", deleteAccessCookie.toString());
        response.addHeader("Set-Cookie", deleteRefreshCookie.toString());

        return ResponseEntity.noContent().build();
    }

    //내가 작성한 게시글 조회
    // URL: /users/me/posts
    @GetMapping("/users/me/posts")
    public ResponseEntity<Page<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Long userId = userDetails.getUserId();

        Page<PostResponseDto> response = postService.getMyPostList(userId, pageable);
        return ResponseEntity.ok(response);
    }

    //내가 작성한 댓글 조회
    // URL: /users/me/comments
    @GetMapping("/users/me/comments")
    public ResponseEntity<Page<CommentResponseDto>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = userDetails.getUserId();

        Page<CommentResponseDto> response = commentService.getMyComments(userId, pageable);
        return ResponseEntity.ok(response);
    }

    // 크리에이터 계좌 등록
    @PostMapping("/users/me/account")
    public ResponseEntity<?> registerAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatorAccountRequestDto requestDto
    ) {

        Long userId = userDetails.getUserId();

        userService.registerAccount(userId, requestDto);

        return ResponseEntity.ok().build();
    }

    // 크리에이터 계좌 수정
    @PatchMapping("/users/me/account")
    public ResponseEntity<?> updateAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatorAccountRequestDto requestDto
    ) {

        Long userId = userDetails.getUserId();

        userService.updateAccount(userId, requestDto);

        return ResponseEntity.ok().build();
    }
}