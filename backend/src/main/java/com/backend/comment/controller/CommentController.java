package com.backend.comment.controller;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;
import com.backend.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    //댓글 생성 - 일단 customuserdetails를 사용한다는 가정하에
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L;
        Long userId = userDetails.getId();
        CommentResponseDto response = commentService.create(postId, userId, request);

        return ResponseEntity.created(URI.create("/comments/" + response.id())).body(response);
    }

    //댓글 수정
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> update(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L;
        Long userId = userDetails.getId();
        CommentResponseDto response = commentService.update(commentId, userId, request);
        return ResponseEntity.ok(response);
    }

    //댓글 삭제 (하드 삭제)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L;
        Long userId = userDetails.getId();
        commentService.delete(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    //댓글 조회
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable Long postId
    ) {
        List<CommentResponseDto> response = commentService.getComments(postId);
        return ResponseEntity.ok(response);
    }

}