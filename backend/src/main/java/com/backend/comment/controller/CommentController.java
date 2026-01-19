package com.backend.comment.controller;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;
import com.backend.comment.service.CommentService;
import com.backend.global.util.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 생성
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getUserId();

        CommentResponseDto response = commentService.create(postId, userId, request);

        return ResponseEntity.created(URI.create("/comments/" + response.id())).body(response);
    }

    /**
     * 댓글 수정
     */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> update(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getUserId();

        CommentResponseDto response = commentService.update(commentId, userId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 삭제 (하드 삭제)
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getUserId();

        commentService.delete(commentId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 조회
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 비로그인(null) 처리
        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;

        // 서비스 호출 시 currentUserId 전달
        Page<CommentResponseDto> response = commentService.getComments(postId, currentUserId, pageable);
        return ResponseEntity.ok(response);
    }
}