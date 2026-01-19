package com.backend.auth.controller;

import com.backend.auth.dto.*;
import com.backend.auth.service.AuthService;
import com.backend.global.exception.AuthErrorCode;
import com.backend.global.exception.common.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        TokenResponseDto result = authService.login(request.email(), request.password());

        // accessToken: HttpOnly 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", result.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 30)
                .secure(false)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", result.refreshToken())
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
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

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
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        TokenResponseDto refresh = authService.refresh(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", refresh.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 30)
                .secure(false)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refresh.refreshToken())
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(60L * 60 * 24 * 14)
                .secure(false)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok().build();
    }

    // 닉네임 중복 체크
    @PostMapping("/check-duplication")
    public ResponseEntity<Boolean> checkDuplication(@RequestBody String nickname) {
        boolean checkDuplication = authService.checkDuplication(nickname);
        return ResponseEntity.ok(checkDuplication);
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequestDto requestDto,
            HttpServletResponse response
    ) {
        authService.signup(requestDto);

        // 회원가입 후 자동 로그인
        TokenResponseDto result = authService.login(requestDto.email(), requestDto.password());

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
        return ResponseEntity.status(201).build();
    }
}
