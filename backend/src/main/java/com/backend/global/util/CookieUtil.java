package com.backend.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${JWT_ACCESS_TOKEN_MS}")
    private long accessTokenMs;

    @Value("${JWT_REFRESH_TOKEN_MS}")
    private long refreshTokenMs;

    public void setTokens(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {
        response.addHeader("Set-Cookie", buildAccessCookie(accessToken).toString());
        response.addHeader("Set-Cookie", buildRefreshCookie(refreshToken).toString());
    }

    public void clearTokens(HttpServletResponse response) {
        response.addHeader("Set-Cookie", deleteAccessCookie().toString());
        response.addHeader("Set-Cookie", deleteRefreshCookie().toString());
    }


    private ResponseCookie buildAccessCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(msToSeconds(accessTokenMs))
                .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(msToSeconds(refreshTokenMs))
                .build();
    }

    private ResponseCookie deleteAccessCookie() {
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    private ResponseCookie deleteRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }

    private long msToSeconds(long ms) {
        return Math.max(1, ms / 1000);
    }
}
