package com.backend.like.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.like.entity.LikeTargetType;
import com.backend.like.service.LikeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    /**
     * 게시글 좋아요
     */
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<?> togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(
                likeService.toggle(
                        userDetails.getUserId(),
                        LikeTargetType.POST,
                        postId
                )
        );
    }

    /**
     * 댓글 좋아요
     */
    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<?> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(
                likeService.toggle(
                        userDetails.getUserId(),
                        LikeTargetType.COMMENT,
                        commentId
                )
        );
    }
}