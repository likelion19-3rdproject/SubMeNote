package com.backend.auth.controller;

import com.backend.auth.dto.LoginRequestDto;
import com.backend.auth.dto.LoginResponseDto;
import com.backend.auth.service.AuthServiceImpl;
import com.backend.auth.service.LoginResult;
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

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request);

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
        return ResponseEntity.ok(new LoginResponseDto(result.refreshToken()));
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
}
