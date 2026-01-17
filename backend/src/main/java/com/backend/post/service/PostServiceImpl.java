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
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.subscribe.service.SubscribeService;
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
    private final SubscribeRepository subscribeRepository;
    private final SubscribeService subscribeService;

    // 게시글 생성
    @Override
    public PostResponseDto create(Long userId, PostCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

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

        post.update(request.title(), request.content(), request.visibility());

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

    // 전체 조회 (피드: 내가 구독한 크리에이터들의 글)
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostList(Long currentUserId, Pageable pageable) {
        // 1. 로그인 체크
        if (currentUserId == null) {
            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
        }

        // 2. 내가 구독 중이고 만료되지 않은 크리에이터 ID 목록 가져오기
        List<Long> subscribedCreatorIds = subscribeRepository.findCreatorIdsByUserId(
                currentUserId
        );

        // 구독한 사람이 한 명도 없으면 빈 페이지 반환
        if (subscribedCreatorIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 3. 구독한 사람들의 글만 조회
        return postRepository.findAllByUserIdIn(subscribedCreatorIds, pageable)
                .map(PostResponseDto::from);
    }

    // 특정 크리에이터의 게시글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getCreatorPostList(Long creatorId, Long currentUserId, Pageable pageable) {
        //로그인 체크
        if (currentUserId == null) {
            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
        }

        return postRepository.findAllByUserId(creatorId, pageable)
                .map(PostResponseDto::from);
    }

    // 상세 조회
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

    // 읽기 권한 검증 로직(상세 조회용)
    private void validateReadPermission(Post post, Long currentUserId) {
        // 1. 로그인 체크
        if (currentUserId == null) {
            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
        }

        // 2. 작성자 본인은 프리패스
        if (post.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 3. 구독 상태 확인 (SubscribeService 위임)
        // 여기서 예외가 터지면 구독자가 아닌 것임
        Subscribe subscribe = subscribeService.validateSubscription(post.getUser().getId(), currentUserId);

        // 4. 유료 글(SUBSCRIBERS_ONLY)인 경우 -> 구독 타입(PAID) 추가 체크
        if (post.getVisibility() == PostVisibility.SUBSCRIBERS_ONLY) {
            if (subscribe.getType() != SubscribeType.PAID) {
                throw new BusinessException(PostErrorCode.PAID_SUBSCRIPTION_REQUIRED);
            }
        }
    }
}