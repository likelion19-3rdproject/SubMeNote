package com.backend.user.controller;

import com.backend.auth.dto.LoginResponseDto;
import com.backend.auth.service.AuthService;
import com.backend.auth.service.LoginResult;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.service.CommentService;
import com.backend.global.CustomUserDetails;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.service.PostService;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.EmailCodeRequestDto;
import com.backend.user.dto.EmailVerifyRequestDto;
import com.backend.user.dto.SignupRequestDto;
import com.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final AuthService authService;


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
            @Valid @RequestBody SignupRequestDto requestDto,
            HttpServletResponse response
    ) {
        userService.signup(requestDto);

        // 회원가입 후 자동 로그인
        LoginResult result = authService.login(requestDto.email(), requestDto.password());

        // accessToken: HttpOnly 쿠키
        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", result.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 30)
                .secure(false)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(60L * 60 * 24 * 14) // 14일
                .secure(false)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 바디로 refreshToken 내려주는 방식은 이제 불필요하지만,
        // 프론트가 이미 이 응답을 기대하고 있으면 일단 유지해도 됨.
        return ResponseEntity.status(201).body(new LoginResponseDto(result.refreshToken()));
    }

    // 회원 탈퇴
    @DeleteMapping("/users/me")
    public ResponseEntity<?> signout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        Long userId = userDetails.getUserId();
        userService.signout(userId);

        // 쿠키 삭제
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .secure(false)
                .sameSite("Lax")
                .build();

        // refreshToken 쿠키 삭제
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(0)
                .secure(false)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", deleteAccessCookie.toString());
        response.addHeader("Set-Cookie", deleteRefreshCookie.toString());

        return ResponseEntity.noContent().build();
    }

    //내가 작성한 게시글 조회
    // URL: /users/me/posts
    @GetMapping("/users/me/posts")
    public ResponseEntity<Page<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Long userId = userDetails.getUserId();

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
        Long userId = userDetails.getUserId();

        Page<CommentResponseDto> response = commentService.getMyComments(userId, pageable);
        return ResponseEntity.ok(response);
    }

}