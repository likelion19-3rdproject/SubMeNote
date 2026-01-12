package com.backend.post.controller;

import com.backend.post.dto.PostCreateRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.dto.PostUpdateRequestDto;
import com.backend.post.service.PostService;
//import com.backend.user.entity.CustomUserDetails; // 임시용
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
    public ResponseEntity<Long> createPost(
            @RequestBody @Valid PostCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L; // 테스트용 임시 처리
        Long postId = postService.createPost(request, userId);
        return ResponseEntity.ok(postId);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L; // 테스트용 임시 처리
        PostResponseDto updatedPost = postService.updatePost(postId, requestDto, userId);
        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L; // 테스트용 임시 처리
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPostList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPostList(pageable));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1. 로그인 했으면 ID 추출, 안 했으면 null
        Long currentUserId = (userDetails != null) ? userDetails.getId() : null;

        // 2. 서비스 호출
        return ResponseEntity.ok(postService.getPost(postId, currentUserId));
    }
}
