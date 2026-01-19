package com.backend.auth.controller;

import com.backend.auth.dto.*;
import com.backend.auth.service.AuthService;
import com.backend.global.exception.domain.AuthErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {

        TokenResponseDto result = authService.login(request.email(), request.password());

        cookieUtil.setTokens(
                response,
                result.accessToken(),
                result.refreshToken()
        );

        // 바디로 refreshToken 내려주는 방식은 이제 불필요하지만,
        // 프론트가 이미 이 응답을 기대하고 있으면 일단 유지해도 됨.
        return ResponseEntity.ok().build();
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        cookieUtil.clearTokens(response);

        return ResponseEntity.ok().build();
    }

    /**
     * 리프레시토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        TokenResponseDto result = authService.refresh(refreshToken);

        cookieUtil.setTokens(
                response,
                result.accessToken(),
                result.refreshToken()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 닉네임 중복 체크
     */
    @PostMapping("/check-duplication")
    public ResponseEntity<Boolean> checkDuplication(@RequestBody String nickname) {

        boolean checkDuplication = authService.checkDuplication(nickname);

        return ResponseEntity.ok(checkDuplication);
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequestDto request,
            HttpServletResponse response
    ) {

        authService.signup(request);

        // 회원가입 후 자동 로그인
        TokenResponseDto result = authService.login(request.email(), request.password());

        cookieUtil.setTokens(
                response,
                result.accessToken(),
                result.refreshToken()
        );

        // 바디로 refreshToken 내려주는 방식은 이제 불필요하지만,
        // 프론트가 이미 이 응답을 기대하고 있으면 일단 유지해도 됨.
        return ResponseEntity.status(201).build();
    }
}
