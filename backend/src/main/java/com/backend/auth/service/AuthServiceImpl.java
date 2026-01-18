package com.backend.auth.service;

import com.backend.auth.dto.TokenResponseDto;
import com.backend.auth.dto.SignupRequestDto;
import com.backend.auth.repository.RefreshTokenStore;
import com.backend.global.exception.AuthErrorCode;
import com.backend.global.exception.MailErrorCode;
import com.backend.global.exception.UserErrorCode;
import com.backend.global.jwt.JwtProvider;
import com.backend.auth.repository.RefreshTokenRepository;
import com.backend.global.exception.common.BusinessException;
import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.email.entity.EmailAuth;
import com.backend.user.entity.User;
import com.backend.email.repository.EmailAuthRepository;
import com.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public TokenResponseDto login(String email, String password) {
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

        //MysqlDB 대신 Redis 사용
        //refreshTokenRepository.save(RefreshToken.of(user.getId(), refreshToken, expiresAt));
        refreshTokenStore.save(user.getId(), refreshToken, jwtProvider.getRefreshTokenMs());


        return new TokenResponseDto(accessToken, refreshToken);
    }

    // 로그아웃 = refreshToken 무효화 (멱등)
    @Override
    @Transactional
    public void logout(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);

        if (!jwtProvider.validate(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
        refreshTokenStore.delete(userId);
    }

    @Override
    @Transactional
    public TokenResponseDto refresh(String refreshToken) {

        // 유효성 확인
        if (!jwtProvider.validate(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        //Redis에 저장된 refresh와 매칭
        String stored = refreshTokenStore.get(userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new BusinessException(AuthErrorCode.NOT_MATCH_REFRESH_TOKEN);
        }


        String newAccess = jwtProvider.createAccessToken(userId);
        String newRefresh = jwtProvider.createRefreshToken(userId);

        //기존 refresh 교체
        refreshTokenStore.save(userId, newRefresh, jwtProvider.getRefreshTokenMs());
        return new TokenResponseDto(newAccess,newRefresh);
    }


    // 닉네임 중복 체크
    @Override
    @Transactional(readOnly = true)
    public boolean checkDuplication(String nickname) {
        if (nickname.trim().isEmpty()) {
            throw new BusinessException(UserErrorCode.NICKNAME_EMPTY);
        }

        if (!nickname.matches("^\\S{2,}$")) {
            throw new BusinessException(UserErrorCode.NICKNAME_INVALID_FORMAT);
        }
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * 회원가입
     * <p>
     * 1. 이미 가입된 이메일인지 체크
     * 2. 이메일 인증 완료 체크
     * 3. 닉네임 중복 체크
     * 4. 역할 선택
     * 5. 회원가입 완료
     */
    @Override
    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new BusinessException(MailErrorCode.EMAIL_DUPLICATED);
        }

        // 이메일 인증 완료 체크
        EmailAuth emailAuth = emailAuthRepository
                .findByEmail(requestDto.email())
                .orElseThrow(() -> new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED));

        if (!emailAuth.isVerified()) {
            throw new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(requestDto.nickname())) {
            throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATED);
        }

        // 역할 선택
        Role userRole = roleRepository.findByRole(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new BusinessException(UserErrorCode.ROLE_NOT_FOUND));

        // 회원가입 완료
        userRepository.save(new User(
                requestDto.email(),
                requestDto.nickname(),
                passwordEncoder.encode(requestDto.password()),
                Set.of(userRole)
        ));

        // EmailAuth 삭제
        emailAuthRepository.delete(emailAuth);
    }



}