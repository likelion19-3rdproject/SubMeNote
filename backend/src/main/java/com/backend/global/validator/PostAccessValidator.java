package com.backend.global.validator;

import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.domain.PostErrorCode;
import com.backend.global.exception.domain.SubscribeErrorCode;
import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.role.entity.RoleEnum;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PostAccessValidator {

    private final SubscribeRepository subscribeRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 접근 권한 검증
     * <br/>
     * - 게시글 상세 조회 시 사용
     * - 댓글 작성/조회 시 사용
     */
    public void validatePostAccess(Post post, Long currentUserId) {

        User user = userRepository.findByIdOrThrow(currentUserId);

        // 1. 작성자 본인은 프리패스
        if (post.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 2. 어드민은 모든 포스트 접근 가능
        if (user.hasRole(RoleEnum.ROLE_ADMIN)) {
            return;
        }

        // 3. 구독 검증
        Subscribe subscribe = validateSubscription(post.getUser().getId(), currentUserId);

        // 4. 유료 게시글인 경우 유료 구독 확인
        if (post.getVisibility() == PostVisibility.SUBSCRIBERS_ONLY) {
            if (subscribe.getType() != SubscribeType.PAID) {
                throw new BusinessException(PostErrorCode.PAID_SUBSCRIPTION_REQUIRED);
            }
        }
    }

    /**
     * 구독 여부 및 만료 확인
     */
    private Subscribe validateSubscription(Long creatorId, Long subscriberId) {
        // 1. 구독 정보 조회
        Subscribe subscribe = subscribeRepository
                .findByUser_IdAndCreator_Id(subscriberId, creatorId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.SUBSCRIPTION_REQUIRED));

        // 2. 유료 구독자 만료 체크
        // 만료일이 존재하고(null 아님), 현재 날짜보다 이전이면(isBefore) -> 만료됨
        if (subscribe.getExpiredAt() != null
                && subscribe.getExpiredAt().isBefore(LocalDate.now())) {
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }

        // 3. 무료 구독자(또는 날짜 없는 구독) 취소 체크
        // 만료일이 없는데(null), 상태가 취소(CANCELED)라면 -> 접근 불가
        if (subscribe.getExpiredAt() == null && subscribe.getStatus() == SubscribeStatus.CANCELED) {
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }

        return subscribe;
    }
}
