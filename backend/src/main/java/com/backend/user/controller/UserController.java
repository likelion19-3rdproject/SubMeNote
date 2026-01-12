package com.backend.user.controller;

import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.service.CommentService;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.service.PostServiceImpl;
import com.backend.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final PostServiceImpl postService;
    private final CommentService commentService;

    //내가 작성한 게시글 조회
    // URL: /users/me/posts
    @GetMapping("/me/posts")
    public ResponseEntity<Page<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Long userId = userDetails.getId();

        Page<PostResponseDto> response = postService.getMyPostList(userId, pageable);
        return ResponseEntity.ok(response);
    }

    //내가 작성한 댓글 조회
    // URL: /users/me/comments
    @GetMapping("/me/comments")
    public ResponseEntity<Page<CommentResponseDto>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
//        Long userId = (userDetails != null) ? userDetails.getId() : 1L;
         Long userId = userDetails.getId();

        Page<CommentResponseDto> response = commentService.getMyComments(userId, pageable);
        return ResponseEntity.ok(response);
    }
}