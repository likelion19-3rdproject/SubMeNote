package com.backend.post.service.impl;

import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.domain.PostErrorCode;
import com.backend.global.validator.RoleValidator;
import com.backend.like.entity.LikeTargetType;
import com.backend.like.service.LikeService;
import com.backend.post.dto.PostRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.entity.Post;
import com.backend.post.repository.PostRepository;
import com.backend.global.validator.PostAccessValidator;
import com.backend.post.service.PostService;
import com.backend.role.entity.RoleEnum;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubscribeRepository subscribeRepository;
    private final LikeService likeService;
    private final PostAccessValidator postAccessValidator;
    private final RoleValidator roleValidator;

    /**
     * 게시글 생성
     */
    @Override
    @Transactional
    public PostResponseDto create(Long userId, PostRequestDto request) {

        User creator = roleValidator.validateCreator(userId);

        Post post = Post.create(
                request.title(),
                request.content(),
                request.visibility(),
                creator
        );

        postRepository.save(post);

        return PostResponseDto.from(post);
    }

    /**
     * 게시글 수정
     */
    @Override
    @Transactional
    public PostResponseDto update(Long postId, Long userId, PostRequestDto request) {

        User user = userRepository.findByIdOrThrow(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException(PostErrorCode.POST_UPDATE_FORBIDDEN);
        }

        post.update(request.title(), request.content(), request.visibility());

        return PostResponseDto.from(post);
    }

    /**
     * 게시글 삭제
     */
    @Override
    @Transactional
    public void delete(Long postId, Long userId) {

        User user = userRepository.findByIdOrThrow(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!isAdminOrOwner(user, post)) {
            throw new BusinessException(PostErrorCode.POST_DELETE_FORBIDDEN);
        }

        postRepository.delete(post);
    }

    /**
     * 내가 구독한 크리에이터들의 게시글 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostList(Long currentUserId, Pageable pageable) {

        User user = userRepository.findByIdOrThrow(currentUserId);

        // 2. 어드민인 경우 모든 포스트 조회
        if (user.hasRole(RoleEnum.ROLE_ADMIN)) {
            return postRepository
                    .findAll(pageable)
                    .map(post -> toDto(post, currentUserId));
        }

        // 3. 일반 사용자는 내가 구독 중이고 만료되지 않은 크리에이터 ID 목록 가져오기
        List<Long> subscribedCreatorIds = subscribeRepository
                .findCreatorIdsByUserId(currentUserId);

        // 구독한 사람이 한 명도 없으면 빈 페이지 반환
        if (subscribedCreatorIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 4. 구독한 사람들의 글만 조회
        // like로 인해서 바꿈
        return postRepository
                .findAllByUserIdIn(subscribedCreatorIds, pageable)
                .map(post -> toDto(post, currentUserId));
    }

    /**
     * 구독한 크리에이터 게시글 검색
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> searchSubscribedPosts(Long currentUserId, String keyword, Pageable pageable) {

        User user = userRepository.findByIdOrThrow(currentUserId);

        // 어드민인 경우 전체 포스트에서 검색
        if (user.hasRole(RoleEnum.ROLE_ADMIN)) {
            return postRepository
                    .findAllByKeyword(keyword, pageable)
                    .map(post -> toDto(post, currentUserId));
        }

        // 일반 사용자는 내가 구독중인 크리에이터 ID 목록 가져오기
        List<Long> subscribedCreatorIds = subscribeRepository
                .findCreatorIdsByUserId(currentUserId);

        // 구독한 사람이 한 명도 없으면 빈 페이지 반환
        if (subscribedCreatorIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 구독한 크리에이터들의 게시글 검색
        return postRepository
                .findAllByUserIdInAndKeyword(
                        subscribedCreatorIds,
                        keyword,
                        pageable
                )
                .map(post -> toDto(post, currentUserId));
    }

    /**
     * 특정 크리에이터 게시글 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getCreatorPostList(Long creatorId, Long currentUserId, Pageable pageable) {

        // like로 인한 변경
        return postRepository
                .findAllByUserId(creatorId, pageable)
                .map(post -> toDto(post, currentUserId));
    }

    /**
     * 게시글 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId, Long currentUserId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        //  권한 검증 메서드 호출
        postAccessValidator.validatePostAccess(post, currentUserId);

        // like로 인한 변경
        return toDto(post, currentUserId);
    }

    /**
     * 내가 작성한 게시글 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getMyPostList(Long userId, Pageable pageable) {

        return postRepository
                .findAllByUserId(userId, pageable)
                // like로 인한 변경
                .map(post -> toDto(post, userId));
    }

    // =================================================================
    // 내부 검증 로직
    // =================================================================

    // 관리자 또는 작성자인지 체크
    private boolean isAdminOrOwner(User user, Post post) {

        if (user.getRole() == null || user.getRole().isEmpty()) {
            return false;
        }

        boolean isAdmin = user
                .getRole()
                .stream()
                .anyMatch(role -> role != null && role.getRole() == RoleEnum.ROLE_ADMIN);

        return isAdmin || post.getUser().getId().equals(user.getId());
    }

    // like
    private PostResponseDto toDto(Post post, Long currentUserId) {

        long likeCount = likeService.count(LikeTargetType.POST, post.getId());
        boolean likedByMe = likeService.likedByMe(currentUserId, LikeTargetType.POST, post.getId());

        return new PostResponseDto(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getTitle(),
                post.getContent(),
                post.getVisibility(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likeCount,
                likedByMe
        );
    }
}