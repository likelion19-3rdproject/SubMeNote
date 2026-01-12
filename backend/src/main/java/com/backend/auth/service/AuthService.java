package com.backend.auth.service;

import com.backend.auth.dto.LoginRequestDto;

public interface AuthService {
    LoginResult login(String email, String password);

    void logout(String refreshToken);
}
