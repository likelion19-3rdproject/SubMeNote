package com.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


public class JwtProvider {
    private final SecretKey key;
    private final long accessTokenMs;
    private final long refreshTokenMs;

    public JwtProvider(String secret, long accessTokenMs, long refreshTokenMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMs = accessTokenMs;
        this.refreshTokenMs = refreshTokenMs;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenMs);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenMs);
    }


    private String createToken(Long userId, long ttlMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //  추가 1) 토큰 검증
    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    //토큰에서 userId(subject) 추출
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }
    public long getRefreshTokenMs() {
        return refreshTokenMs;
    }
}