package com.backend.user.service;

import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.common.ErrorCode;
import com.backend.global.util.MailSender;
import com.backend.role.entity.Role;
import com.backend.role.repository.RoleRepository;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.EmailCodeRequestDto;
import com.backend.user.dto.EmailVerifyRequestDto;
import com.backend.user.dto.SignupRequestDto;
import com.backend.user.entity.EmailAuth;
import com.backend.user.entity.User;
import com.backend.user.repository.EmailAuthRepository;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final RoleRepository roleRepository;
    private final MailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // TODO
    @Override
    @Transactional(readOnly = true)
    public Page<CreatorResponseDto> listAllCreators(int page, int size) {
        return null;
    }

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
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 현재 인증 진행 중인지 체크
        if (emailAuthRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_IN_PROGRESS_AUTHENTICATION);
        }

        // 인증번호 생성 및 메일 전송
        String authCode = mailSender.sendMessage(email);

        if (authCode == null) {
            throw new BusinessException(ErrorCode.EMAIL_SENDING_ERROR);
        }

        emailAuthRepository.save(new EmailAuth(email, authCode));
    }

    /**
     * 이메일 인증 코드 재전송
     *
     * 1. 인증번호 저장소에 존재한다면 삭제
     * 2. 인증번호 생성 및 메일 전송
     */
    @Override
    @Transactional
    public void resendAuthCode(EmailCodeRequestDto requestDto) {
        String email = requestDto.email();

        // 인증번호 저장소에 존재한다면 삭제
        emailAuthRepository.findByEmail(email)
                .ifPresent(emailAuthRepository::delete);

        // 인증번호 생성 및 메일 전송
        String authCode = mailSender.sendMessage(email);

        if (authCode == null) {
            throw new BusinessException(ErrorCode.EMAIL_SENDING_ERROR);
        }

        if (!emailAuthRepository.existsByEmail(email)) {
            emailAuthRepository.save(new EmailAuth(email, authCode));
        }
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
        // 인증 정보 존재 여부 확인
        EmailAuth auth = emailAuthRepository
                .findByEmail(requestDto.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_AUTHCODE));

        // 인증 시간 유효 여부 확인
        if (auth.isExpired()) {
            emailAuthRepository.delete(auth);
            throw new BusinessException(ErrorCode.AUTHENTICATION_EXPIRED);
        }

        // 인증번호 일치 확인
        if (!auth.getAuthCode()
                .equals(requestDto.authCode())) {
            return false;
        }

        // 인증 완료 처리
        auth.markAsVerified();
        return true;
    }

    // 닉네임 중복 체크
    @Override
    @Transactional(readOnly = true)
    public boolean checkDuplication(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * 회원가입
     * <p>
     * 1. 이미 가입된 이메일인지 체크
     * 2. 이메일 인증 완료 체크
     * 3. 닉네임 중복 체크
     * 4. 역할 선택
     * 5. 회원가입 완료
     */
    // TODO: 역할 선택 추가
    @Override
    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 이메일 인증 완료 체크
        EmailAuth emailAuth = emailAuthRepository
                .findByEmail(requestDto.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED));

        if (!emailAuth.isVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(requestDto.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 역할 선택
        Role userRole = roleRepository
                .findByRole(requestDto.role())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        // 회원가입 완료
        userRepository.save(new User(
                requestDto.email(),
                requestDto.nickname(),
                passwordEncoder.encode(requestDto.password()),
                Set.of(userRole)
        ));

        // EmailAuth 삭제
        emailAuthRepository.delete(emailAuth);
    }
}
