package com.backend.email.controller;

import com.backend.email.dto.EmailCodeRequestDto;
import com.backend.email.dto.EmailVerifyRequestDto;
import com.backend.email.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class MailController {

    private final MailService mailService;

    // 이메일 인증 코드 전송
    @PostMapping("/send")
    public ResponseEntity<?> sendAuthCode(
            @Valid @RequestBody EmailCodeRequestDto requestDto
    ) {
        mailService.sendAuthCode(requestDto);
        return ResponseEntity.noContent().build();
    }

    // 이메일 인증 코드 재전송
    @PostMapping("/resend")
    public ResponseEntity<?> resendAuthCode(
            @Valid @RequestBody EmailCodeRequestDto requestDto
    ) {
        mailService.resendAuthCode(requestDto);
        return ResponseEntity.noContent().build();
    }

    // 이메일 인증 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyAuthCode(
            @Valid @RequestBody EmailVerifyRequestDto requestDto
    ) {
        boolean validated = mailService.validateAuthCode(requestDto);
        return ResponseEntity.ok(validated);
    }
}
