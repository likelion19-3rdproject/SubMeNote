package com.backend.user.controller;

import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.service.CommentService;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final PostService postService;
    private final CommentService commentService;

    //내가 작성한 게시글 조회
    // URL: /users/me/posts
    @GetMapping("/me/posts")
    public ResponseEntity<List<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getId();

        List<PostResponseDto> response = postService.getMyPostList(email);
        return ResponseEntity.ok(response);
    }

    //내가 작성한 댓글 조회
    // URL: /users/me/comments
    @GetMapping("/me/comments")
    public ResponseEntity<List<CommentResponseDto>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L;
         Long userId = userDetails.getId();

        List<CommentResponseDto> response = commentService.getMyComments(userId);
        return ResponseEntity.ok(response);
    }
}