package com.backend.auth.service;

import com.backend.global.exception.AuthErrorCode;
import com.backend.auth.dto.LoginRequestDto;
import com.backend.auth.entity.RefreshToken;
import com.backend.auth.jwt.JwtProvider;
import com.backend.auth.repository.RefreshTokenRepository;
import com.backend.global.exception.common.BusinessException;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public LoginResult login(String email, String password) {
        // 1) 이메일로 사용자 조회 (없으면 400)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        // 2) 비밀번호 검증 (틀리면 400)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 3) 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // (옵션) 유저당 refresh 1개만 유지하고 싶으면 기존 전부 삭제
        // refreshTokenRepository.deleteAllByUserId(user.getId());

        // 4) refreshToken 저장
        Instant expiresAt = Instant.now().plusMillis(jwtProvider.getRefreshTokenMs());
        refreshTokenRepository.save(RefreshToken.of(user.getId(), refreshToken, expiresAt));

        return new LoginResult(accessToken, refreshToken);
    }

    // 로그아웃 = refreshToken 무효화 (멱등)
    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}