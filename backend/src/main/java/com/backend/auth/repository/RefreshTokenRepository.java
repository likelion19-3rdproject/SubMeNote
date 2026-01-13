package com.backend.auth.repository;

import com.backend.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 재발급(/reissue)
    Optional<RefreshToken> findByToken(String token);

    //로그아웃(RefreshToken 무효화)
    void deleteByToken(String token);

    // 유저당 refresh 1개만 유지(다중 로그인 차단 느낌)
    void deleteAllByUserId(Long userId);
}