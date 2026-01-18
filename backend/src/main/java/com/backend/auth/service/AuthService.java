package com.backend.auth.service;

import com.backend.auth.dto.LoginResultDto;
import com.backend.auth.dto.SignupRequestDto;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    LoginResultDto login(String email, String password);

    void logout(String refreshToken);

    @Transactional
    void refresh(String refreshToken);

    boolean checkDuplication(String nickname);

    void signup(SignupRequestDto requestDto);

}
