package com.backend.auth.service;

import com.backend.auth.dto.TokenResponseDto;
import com.backend.auth.dto.SignupRequestDto;

public interface AuthService {

    TokenResponseDto login(String email, String password);

    void logout(String refreshToken);

    TokenResponseDto refresh(String refreshToken);

    boolean checkDuplication(String nickname);

    void signup(SignupRequestDto requestDto);

}
