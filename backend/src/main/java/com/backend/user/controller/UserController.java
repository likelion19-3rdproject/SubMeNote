package com.backend.user.controller;

import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.EmailCodeRequestDto;
import com.backend.user.dto.EmailVerifyRequestDto;
import com.backend.user.dto.SignupRequestDto;
import com.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final PostServiceImpl postService;
    private final CommentService commentService;


    // FIXME: 전체 크리에이터 조회 (구독자 수 표기?)
    @GetMapping
    public ResponseEntity<Page<CreatorResponseDto>> home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CreatorResponseDto> creators = userService.listAllCreators(page, size);
        return ResponseEntity.ok(creators);
    }

    // 이메일 인증 코드 전송
        @PostMapping("/email/send")
        public ResponseEntity<?> sendAuthCode(
                @Valid @RequestBody EmailCodeRequestDto requestDto
    ) {
            userService.sendAuthCode(requestDto);
            return ResponseEntity.noContent().build();
        }

        // 이메일 인증 코드 재전송
        @PostMapping("/email/resend")
        public ResponseEntity<?> resendAuthCode(
                @Valid @RequestBody EmailCodeRequestDto requestDto
    ) {
            userService.resendAuthCode(requestDto);
        return ResponseEntity.noContent().build();
    }

    // 이메일 인증 코드 검증
    @PostMapping("/email/verify")
    public ResponseEntity<Boolean> verifyAuthCode(
            @Valid @RequestBody EmailVerifyRequestDto requestDto
    ) {
        boolean validated = userService.validateAuthCode(requestDto);
        return ResponseEntity.ok(validated);
    }

    // 닉네임 중복 체크
    @PostMapping("/auth/check-duplication")
    public ResponseEntity<Boolean> checkDuplication(@RequestBody String nickname) {
        boolean checkDuplication = userService.checkDuplication(nickname);
        return ResponseEntity.ok(checkDuplication);
    }

    // 회원가입
    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        // FIXME: 회원가입 후 자동 로그인
        return ResponseEntity.status(201).build();
    }

    // 회원 탈퇴
    @DeleteMapping("/users/me")
    public ResponseEntity<?> signout(
            @Valid @RequestBody String nickname
    ) {
        log.info("controller nickname: {}", nickname);
        userService.signout(nickname);
        return ResponseEntity.noContent().build();
    }

    //내가 작성한 게시글 조회
    // URL: /users/me/posts
    @GetMapping("/users/me/posts")
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
    @GetMapping("/users/me/comments")
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