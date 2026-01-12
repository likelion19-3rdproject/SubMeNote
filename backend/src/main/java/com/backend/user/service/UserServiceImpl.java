package com.backend.user.service;

import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.UserErrorCode;
import com.backend.global.util.MailSender;
import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final RoleRepository roleRepository;
    private final MailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    /**
     * CREATOR 목록 표시
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CreatorResponseDto> listAllCreators(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> creators = userRepository.findByRoleEnum(RoleEnum.ROLE_CREATOR, pageable);

        return creators.map(creator -> new CreatorResponseDto(creator.getNickname()));
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
            throw new BusinessException(UserErrorCode.EMAIL_DUPLICATED);
        }

        // 현재 인증 진행 중인지 체크
        if (emailAuthRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.EMAIL_IN_PROGRESS_AUTHENTICATION);
        }

        // 인증번호 생성 및 메일 전송
        String authCode = mailSender.sendMessage(email);

        if (authCode == null) {
            throw new BusinessException(UserErrorCode.EMAIL_SENDING_ERROR);
        }

        emailAuthRepository.save(new EmailAuth(email, authCode));
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
        emailAuthRepository.findByEmail(email)
                .ifPresent(emailAuthRepository::delete);

        // 인증번호 생성 및 메일 전송
        String authCode = mailSender.sendMessage(email);

        if (authCode == null) {
            throw new BusinessException(UserErrorCode.EMAIL_SENDING_ERROR);
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
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_AUTHCODE));

        // 인증 시간 유효 여부 확인
        if (auth.isExpired()) { // 유효시간이 만료되었다면
            emailAuthRepository.delete(auth); // 저장소에서 삭제
            throw new BusinessException(UserErrorCode.AUTHENTICATION_EXPIRED);
        }

        // 인증번호 일치 확인
        if (!auth.getAuthCode().equals(requestDto.authCode())) {
            throw new BusinessException(UserErrorCode.INVALID_AUTH_CODE);
        }

        // 인증 완료 처리
        auth.markAsVerified();
        return true;
    }

    // 닉네임 중복 체크
    @Override
    @Transactional(readOnly = true)
    public boolean checkDuplication(String nickname) {
        if (nickname.isBlank()) {
            throw new BusinessException(UserErrorCode.NICKNAME_EMPTY);
        }

        if (!nickname.matches("^\\S{2,}$")) {
            throw new BusinessException(UserErrorCode.NICKNAME_INVALID_FORMAT);
        }
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
    @Override
    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new BusinessException(UserErrorCode.EMAIL_DUPLICATED);
        }

        // 이메일 인증 완료 체크
        EmailAuth emailAuth = emailAuthRepository
                .findByEmail(requestDto.email())
                .orElseThrow(() -> new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED));

        if (!emailAuth.isVerified()) {
            throw new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(requestDto.nickname())) {
            throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATED);
        }

        // 역할 선택
        Role userRole = roleRepository
                .findByRole(requestDto.role())
                .orElseThrow(() -> new BusinessException(UserErrorCode.ROLE_NOT_FOUND));

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

    /**
     * 회원 탈퇴
     * <p>
     * 1. 사용자 존재 여부 확인
     * 2. refreshToken 삭제
     * 3. 사용자 삭제
     */
    // FIXME: 로그인 상태 확인 / 구독 상태 확인 / 리프레시 토큰 삭제
    @Override
    @Transactional
    public void signout(String nickname) {
        log.info("service nickname: {}", nickname);
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // FIXME: 리프레시 토큰 삭제

        userRepository.delete(user);
    }
}
