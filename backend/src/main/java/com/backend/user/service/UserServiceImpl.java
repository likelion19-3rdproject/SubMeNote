package com.backend.user.service;

import com.backend.auth.repository.RefreshTokenRepository;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.UserErrorCode;
import com.backend.role.entity.RoleEnum;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.entity.User;
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

            if (activeSubscriberCount > 0) {}
            throw new BusinessException(UserErrorCode.CREATOR_HAS_ACTIVE_SUBSCRIBERS);
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
}
