package com.backend.auth.service;

import com.backend.auth.dto.LoginRequestDto;

public interface AuthService {
    LoginResult login(LoginRequestDto requestDto);

    void logout(String refreshToken);
}
