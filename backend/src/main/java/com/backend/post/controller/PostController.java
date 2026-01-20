package com.backend.post.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.post.dto.PostRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @RequestBody @Valid PostRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getUserId();

        PostResponseDto createdPost = postService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    /**
     * 게시글 수정
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getUserId();

        PostResponseDto updatedPost = postService.update(postId, userId, requestDto);

        return ResponseEntity.ok(updatedPost);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getUserId();

        postService.delete(postId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 내가 구독한 크리에이터들의 글 조회
     */
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPostList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        // 비로그인(null)이면 서비스로 넘김 -> 서비스에서 BusinessException(LOGIN_REQUIRED) 발생
        Long currentUserId = userDetails.getUserId();

        return ResponseEntity.ok(postService.getPostList(currentUserId, pageable));
    }

    /**
     * 구독한 크리에이터 게시글 검색 목록 조회
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponseDto>> searchSubscribedPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Long currentUserId = userDetails.getUserId();

        return ResponseEntity.ok(postService.searchSubscribedPosts(currentUserId, keyword, pageable));
    }

    /**
     * 특정 크리에이터의 게시글 목록 조회 (크리에이터 홈)
     */
    @GetMapping("/creators/{creatorId}")
    public ResponseEntity<Page<PostResponseDto>> getCreatorPostList(
            @PathVariable Long creatorId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        // 비로그인(null)이면 서비스로 넘김 -> 서비스에서 예외 처리
        Long currentUserId = userDetails.getUserId();

        return ResponseEntity.ok(postService.getCreatorPostList(creatorId, currentUserId, pageable));
    }

    /**
     * 게시글 상세 조회 (권한에 따른 열람 제어)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long currentUserId = userDetails.getUserId();

        return ResponseEntity.ok(postService.getPost(postId, currentUserId));
    }
}
