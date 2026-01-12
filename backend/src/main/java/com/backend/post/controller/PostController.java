package com.backend.post.controller;

import com.backend.global.CustomUserDetails;
import com.backend.post.dto.PostCreateRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.dto.PostUpdateRequestDto;
import com.backend.post.service.PostService;
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
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @RequestBody @Valid PostCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        PostResponseDto createdPost = postService.create(userId, request);
        return ResponseEntity.ok(createdPost);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        PostResponseDto updatedPost = postService.update(postId, userId, requestDto);
        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        postService.delete(postId, userId);
        return ResponseEntity.noContent().build();
    }

    // 내가 구독한 크리에이터들의 글 조회
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPostList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 비로그인 상태라면 서비스 진입 전 차단하거나, 서비스에서 예외 처리
        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;

        return ResponseEntity.ok(postService.getPostList(currentUserId, pageable));
    }

    // 특정 크리에이터의 게시글 목록 조회 (크리에이터 홈)
    @GetMapping("/creators/{creatorId}")
    public ResponseEntity<Page<PostResponseDto>> getCreatorPostList(
            @PathVariable Long creatorId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 비로그인 상태면 null 전달 -> 서비스에서 "로그인 필요" 예외 발생시킴
        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;

        return ResponseEntity.ok(postService.getCreatorPostList(creatorId, currentUserId, pageable));
    }

    // 게시글 상세 조회 (권한에 따른 열람 제어)
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 비로그인 상태면 null 전달 -> 서비스에서 로직 처리
        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;

        return ResponseEntity.ok(postService.getPost(postId, currentUserId));
    }
}
