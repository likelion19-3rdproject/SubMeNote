package com.backend.auth.service;

import com.backend.auth.dto.LoginResultDto;
import com.backend.auth.dto.SignupRequestDto;

public interface AuthService {
    LoginResultDto login(String email, String password);

    void logout(String refreshToken);

    boolean checkDuplication(String nickname);

    void signup(SignupRequestDto requestDto);

}
