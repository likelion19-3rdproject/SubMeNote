package com.backend.email.service;

import com.backend.email.dto.EmailCodeRequestDto;
import com.backend.email.dto.EmailVerifyRequestDto;

public interface MailService {
    void sendAuthCode(EmailCodeRequestDto requestDto);

    void resendAuthCode(EmailCodeRequestDto requestDto);

    boolean validateAuthCode(EmailVerifyRequestDto requestDto);

}
