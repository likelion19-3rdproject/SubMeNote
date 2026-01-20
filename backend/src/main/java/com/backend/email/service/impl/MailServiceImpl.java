package com.backend.email.service.impl;

import com.backend.email.dto.EmailCodeRequestDto;
import com.backend.email.dto.EmailVerifyRequestDto;
import com.backend.email.repository.EmailAuthStore;
import com.backend.email.service.MailService;
import com.backend.global.exception.domain.MailErrorCode;
import com.backend.global.exception.domain.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.util.MailSender;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final MailSender mailSender;
    private final EmailAuthStore emailAuthStore;
    private final UserRepository userRepository;

    private static final long AUTH_TTL_MS = 5 * 60 * 1000L;

    /**
     * 이메일 인증코드 전송
     * <p>
     * 1. 이미 가입된 이메일인지 체크
     * 2. 현재 인증 진행 중인지 체크
     * 3. 인증번호 생성 및 메일 전송
     */
    @Override
    @Transactional
    public void sendAuthCode(EmailCodeRequestDto requestDto) {

        String email = requestDto.email();

        // 이미 가입된 이메일인지 체크
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(MailErrorCode.EMAIL_DUPLICATED);
        }

        // 현재 인증 진행 중인지 체크
        if (emailAuthStore.existsCode(email)) {
            throw new BusinessException(MailErrorCode.EMAIL_IN_PROGRESS_AUTHENTICATION);
        }

        // 인증번호 생성 및 메일 전송
        String authCode = mailSender.sendMessage(email);

        if (authCode == null) {
            throw new BusinessException(MailErrorCode.EMAIL_SENDING_ERROR);
        }

        emailAuthStore.saveCode(email, authCode, AUTH_TTL_MS);
    }

    /**
     * 이메일 인증 코드 재전송
     * <p>
     * 1. 인증번호 저장소에 존재한다면 삭제
     * 2. 인증번호 생성 및 메일 전송
     */
    @Override
    @Transactional
    public void resendAuthCode(EmailCodeRequestDto requestDto) {

        String email = requestDto.email();

        // 인증번호 저장소에 존재한다면 삭제
        emailAuthStore.deleteCode(email);
        emailAuthStore.deleteVerified(email);

        // 인증번호 생성 및 메일 전송
        String authCode = mailSender.sendMessage(email);

        if (authCode == null) {
            throw new BusinessException(MailErrorCode.EMAIL_SENDING_ERROR);
        }

        emailAuthStore.saveCode(email, authCode, AUTH_TTL_MS);
    }

    /**
     * 이메일 인증 코드 검증
     * <p>
     * 1. 인증 정보 존재 여부 확인
     * 2. 인증 시간 유효 여부 확인
     * 3. 인증번호 일치 확인
     * 4. 인증 완료 처리
     */
    @Override
    @Transactional
    public boolean validateAuthCode(EmailVerifyRequestDto requestDto) {

        String email = requestDto.email();
        String savedCode = emailAuthStore.getCode(email);

        // 인증 정보 존재 여부 확인
        if (savedCode == null) {
            throw new BusinessException(MailErrorCode.AUTHENTICATION_EXPIRED);
        }

        // 인증 시간 유효 여부 확인 (Redis 에서는 필요 없음)

        // 인증번호 일치 확인
        if (!savedCode.equals(requestDto.authCode())) {
            throw new BusinessException(UserErrorCode.INVALID_AUTH_CODE);
        }

        // 인증 완료 처리
        emailAuthStore.saveVerified(email,AUTH_TTL_MS);
        emailAuthStore.deleteCode(email);
        return true;
    }
}
