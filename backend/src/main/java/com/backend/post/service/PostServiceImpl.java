package com.backend.post.service;

import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.PostErrorCode;
import com.backend.post.dto.PostCreateRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.dto.PostUpdateRequestDto;
import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.post.repository.PostRepository;
import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 생성
    @Override
    public PostResponseDto create(Long userId, PostCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(PostErrorCode.POST_CREATE_FORBIDDEN);
        }

        Post post = Post.create(
                request.title(),
                request.content(),
                request.visibility(),
                user
        );

        postRepository.save(post);
        return PostResponseDto.from(post);
    }

    // 게시글 수정
    @Override
    public PostResponseDto update(Long postId, Long userId, PostUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException(PostErrorCode.POST_UPDATE_FORBIDDEN);
        }

        post.update(
                request.title(),
                request.content(),
                request.visibility()
        );

        return PostResponseDto.from(post);
    }

    // 게시글 삭제
    @Override
    public void delete(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!isAdminOrOwner(user, post)) {
            throw new BusinessException(PostErrorCode.POST_DELETE_FORBIDDEN);
        }

        postRepository.delete(post);
    }

    //전체 조회 내가 구독한 크리에이터들의 글 조회
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostList(Long currentUserId, Pageable pageable) {
//        // 1. 로그인 체크
//        if (currentUserId == null) {
//            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
//
//            // 2. 내가 구독한 크리에이터 ID 목록 가져오기
//            // TODO: 실제로는 subscriptionRepository.findCreatorIdsBySubscriberId(currentUserId); 호출
//            // [임시] 테스트를 위해: 현재 유저는 1번이고, 2번과 3번 유저를 구독했다고 가정
//            List<Long> subscribedCreatorIds = List.of(2L, 3L);
//
//            // 만약 구독한 사람이 한 명도 없을 시 -> 빈 페이지 반환
//            if (subscribedCreatorIds.isEmpty()) {
//                return Page.empty(pageable);
//            }
//
//            // 3. 구독한 사람들의 글만 DB에서 조회 (IN 쿼리)
//            return postRepository.findAllByUserIdIn(subscribedCreatorIds, pageable)
//                    .map(PostResponseDto::from);
//        }
        return null;
    }

    //특정 크리에이터의 게시글 목록 조회 (로그인 + 무료구독 필수)
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getCreatorPostList(Long creatorId, Long currentUserId, Pageable pageable) {
        // 1. 로그인 체크
        if (currentUserId == null) {
            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
        }

        // 2. 구독 체크 (본인이 아니면 확인)
        if (!creatorId.equals(currentUserId)) {
            validateSubscription(creatorId, currentUserId);
        }

        // 3. 목록 반환
        return postRepository.findAllByUserId(creatorId, pageable)
                .map(PostResponseDto::from);
    }

    //상세 조회
    @Override
    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        //  권한 검증 메서드 호출
        validateReadPermission(post, currentUserId);

        return PostResponseDto.from(post);
    }

    // 내가 작성한 게시글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getMyPostList(Long userId, Pageable pageable) {
        return postRepository.findAllByUserId(userId, pageable)
                .map(PostResponseDto::from);
    }

    // =================================================================
    // 내부 검증 로직
    // =================================================================

    // 관리자 또는 작성자인지 체크
    private boolean isAdminOrOwner(User user, Post post) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            return false;
        }

        boolean isAdmin = user.getRole().stream()
                .anyMatch(role -> role != null && role.getRole() == RoleEnum.ROLE_ADMIN);

        return isAdmin || post.getUser().getId().equals(user.getId());
    }

    //읽기 권한 검증 로직 (상세 조회용)
    private void validateReadPermission(Post post, Long currentUserId) {
        // 1. 로그인 체크 (가장 먼저!)
        if (currentUserId == null) {
            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
        }

        // 2. 작성자 본인은 프리패스
        if (post.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 3. 무료 구독(팔로우) 여부 확인
        // (PUBLIC 글이라도 구독 안 했으면 못 봄)
        validateSubscription(post.getUser().getId(), currentUserId);

        // 4. 2차 방어선: 유료(구독자 전용) 글 등급 확인
        if (post.getVisibility() == PostVisibility.SUBSCRIBERS_ONLY) {
            // TODO: 추후 실제 유료 구독 여부 확인 로직 (DB 조회)
            // boolean isPaidSubscriber = subscriptionRepository.isPaid(post.getUser().getId(), currentUserId);

            boolean isPaidSubscriber = false; // [임시] 유료 구독 안 함으로 가정

            if (!isPaidSubscriber) {
                throw new BusinessException(PostErrorCode.PAID_SUBSCRIPTION_REQUIRED);
            }
        }
    }

    // [공통] 구독 여부 확인 메서드
    private void validateSubscription(Long creatorId, Long subscriberId) {
        // TODO: 추후 실제 구독 DB 조회
        // boolean isSubscribed = subscriptionRepository.existsByCreatorIdAndSubscriberId(creatorId, subscriberId);

        boolean isSubscribed = true;

        if (!isSubscribed) {
            throw new BusinessException(PostErrorCode.SUBSCRIPTION_REQUIRED);
        }
    }
}