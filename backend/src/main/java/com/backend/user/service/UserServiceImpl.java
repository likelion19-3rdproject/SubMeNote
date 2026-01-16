package com.backend.user.service;

import com.backend.auth.repository.RefreshTokenRepository;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.UserErrorCode;
import com.backend.role.entity.RoleEnum;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.user.dto.*;
import com.backend.user.entity.Account;
import com.backend.user.entity.ApplicationStatus;
import com.backend.user.entity.CreatorApplication;
import com.backend.user.entity.User;
import com.backend.user.repository.AccountRepository;
import com.backend.user.repository.ApplicationRepository;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SubscribeRepository subscribeRepository;
    private final AccountRepository accountRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * CREATOR 목록 표시
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CreatorResponseDto> listAllCreators(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> creators = userRepository.findByRoleEnum(RoleEnum.ROLE_CREATOR, pageable);

        return creators.map(creator -> new CreatorResponseDto(creator.getId(),creator.getNickname()));
    }

    /**
     * 내 정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        
        return UserResponseDto.from(user);
    }

    /**
     * 회원 탈퇴
     * <p>
     * 탈퇴 제약 조건
     * 1. CREATOR 이면서 활성 구독자가 있으면 탈퇴 불가
     * 2. USER 이면서 유로 구독한 CREATOR가 있으면 탈퇴 불가
     * <p>
     * 1. 사용자 존재 여부 확인
     * 2. refreshToken 삭제
     * 3. 사용자 삭제
     */
    @Override
    @Transactional
    public void signout(Long userId) {
        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // 탈퇴 제약 조건 확인
        validateUserCanDelete(user);

        // 리프레시 토큰 삭제
        refreshTokenRepository.deleteAllByUserId(userId);

        // 사용자 삭제
        userRepository.delete(user);
    }

    // 탈퇴 제약 조건
    private void validateUserCanDelete(User user) {
        // CREATOR 이면서 활성 구독자가 있으면 탈퇴 불가
        if (user.hasRole(RoleEnum.ROLE_CREATOR)) {
            long activeSubscriberCount = subscribeRepository.countByCreator_IdAndStatus(
                    user.getId(),
                    SubscribeStatus.ACTIVE
            );

            if (activeSubscriberCount > 0) {
                throw new BusinessException(UserErrorCode.CREATOR_HAS_ACTIVE_SUBSCRIBERS);
            }
        }

        // USER 이면서 유료 구독한 CREATOR가 있으면 탈퇴 불가
        if (user.hasRole(RoleEnum.ROLE_USER)) {
            long activePaidSubscriptionCount = subscribeRepository.countByUser_IdAndStatusAndType(
                    user.getId(),
                    SubscribeStatus.ACTIVE,
                    SubscribeType.PAID
            );

            if (activePaidSubscriptionCount > 0) {
                throw new BusinessException(UserErrorCode.USER_HAS_PAID_SUBSCRIPTIONS);
            }
        }
    }

    /**
     * 크리에이터 계좌 등록
     * <br/>
     * 1. 존재하는 유저인지 확인
     * 2. 크리에이터인지 확인
     * 3. 이미 등록된 계좌가 있는지 확인
     */
    @Override
    @Transactional
    public void registerAccount(Long userId, AccountRequestDto requestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        if (user.hasAccount()) {
            throw new BusinessException(UserErrorCode.ACCOUNT_ALREADY_REGISTERED);
        }

        Account account = new Account(
                requestDto.bankName(),
                requestDto.accountNumber(),
                requestDto.holderName());

        user.registerAccount(account);

        accountRepository.save(account);
    }

    @Override
    public AccountResponseDto getAccount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        if (user.getAccount() == null) {
            throw new BusinessException(UserErrorCode.ACCOUNT_NOT_FOUND);
        }

        return AccountResponseDto.from(user.getAccount());
    }

    @Override
    @Transactional
    public void updateAccount(Long userId, AccountRequestDto requestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        Account account = user.getAccount();
        if (account == null) {
            throw new BusinessException(UserErrorCode.ACCOUNT_NOT_FOUND);
        }

        account.update(
                requestDto.bankName(),
                requestDto.accountNumber(),
                requestDto.holderName()
        );
    }

    /**
     * 크리에이터 신청
     * <br/>
     * 1. 일반 USER인지 확인
     * 2. 이미 승인 대기 중인 CREATOR 신청이 있는지 확인
     */
    @Override
    @Transactional
    public void applyForCreator(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.hasRole(RoleEnum.ROLE_USER)) {
            throw new BusinessException(UserErrorCode.ONLY_USER_CAN_APPLY_CREATOR);
        }

        if (applicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new BusinessException(UserErrorCode.APPLICATION_ALREADY_PENDING);
        }

        applicationRepository.save(
                new CreatorApplication(user)
        );
    }

    /**
     * 크리에이터 신청 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public CreatorApplicationResponseDto getMyApplication(Long userId) {

        CreatorApplication application = applicationRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.APPLICATION_NOT_FOUND));

        return CreatorApplicationResponseDto.from(application);
    }
}
