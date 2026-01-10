package com.backend.post.controller;

import com.backend.post.dto.PostCreateRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.dto.PostUpdateRequestDto;
import com.backend.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<Long> createPost(
            @RequestBody @Valid PostCreateRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 로그인 기능 완성 전 테스트(로그인 기능 구현후 반드시 삭제)
//        String email = "test@test.com"; // 아까 DB에 넣은 이메일
//        if (userDetails != null) {
//            email = userDetails.getUsername();
//        }
//
//        Long postId = postService.createPost(request, email);

        Long postId = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.ok(postId);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostResponseDto updatedPost =
                postService.updatePost(postId, requestDto, userDetails.getUsername());

        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getPostList() {
        return ResponseEntity.ok(postService.getPostList());
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }
}
