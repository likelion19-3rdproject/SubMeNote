package com.backend.user.service;

import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.EmailCodeRequestDto;
import com.backend.user.dto.EmailVerifyRequestDto;
import com.backend.user.dto.SignupRequestDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<CreatorResponseDto> listAllCreators(int page, int size);

    void sendAuthCode(EmailCodeRequestDto requestDto);

    void resendAuthCode(EmailCodeRequestDto requestDto);

    boolean validateAuthCode(EmailVerifyRequestDto requestDto);

    boolean checkDuplication(String nickname);

    void signup(SignupRequestDto requestDto);

    void signout(String nickname);
}
