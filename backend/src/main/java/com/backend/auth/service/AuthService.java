package com.backend.auth.service;

import com.backend.auth.dto.TokenResponseDto;
import com.backend.auth.dto.SignupRequestDto;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    TokenResponseDto login(String email, String password);

    void logout(String refreshToken);

    @Transactional
    TokenResponseDto refresh(String refreshToken);

    boolean checkDuplication(String nickname);

    void signup(SignupRequestDto requestDto);

}
