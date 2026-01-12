package com.backend.post.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    // private final SubscriptionRepository subscriptionRepository; // 추후 추가

    // 게시글 생성
    public Long createPost(PostCreateRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        Post post = Post.create(
                request.title(),
                request.content(),
                request.visibility(),
                user
        );

        return postRepository.save(post).getId();
    }

    // 게시글 수정
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!isAdminOrOwner(user, post)) {
            throw new SecurityException("권한이 없습니다.");
        }

        post.update(
                request.title(),
                request.content(),
                request.visibility()
        );

        return PostResponseDto.from(post);
    }

    // 게시글 삭제
    public void deletePost(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!isAdminOrOwner(user, post)) {
            throw new SecurityException("권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    //게시글 전체 조회 (목록)
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostList(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(PostResponseDto::from);
    }

    // 게시글 단건 조회 (상세)
    public PostResponseDto getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // ★ 권한 검증 메서드 호출
        validateReadPermission(post, currentUserId);

        return PostResponseDto.from(post);
    }

    // 내가 작성한 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getMyPostList(Long userId, Pageable pageable) {

        return postRepository.findAllByUserId(userId, pageable)
                .map(PostResponseDto::from);
    }

    // 관리자 또는 작성자인지 체크
    private boolean isAdminOrOwner(User user, Post post) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            return false;
        }

        boolean isAdmin = user.getRole().stream()
                .anyMatch(role -> role != null && role.getRole() == RoleEnum.ROLE_ADMIN);

        return isAdmin || post.getUser().getId().equals(user.getId());
    }

    //읽기 권한 검증 로직
    private void validateReadPermission(Post post, Long currentUserId) {
        // 1. Enum 값 비교: PUBLIC이면 누구나 통과
        if (post.getVisibility() == PostVisibility.PUBLIC) {
            return;
        }

        // --- 여기서부터는 비공개(SUBSCRIBERS_ONLY) 영역 ---

        // 2. 로그인 안 한 유저 차단
        if (currentUserId == null) {
            throw new SecurityException("로그인이 필요한 게시글입니다.");
        }

        // 3. 작성자 본인이면 프리패스
        if (post.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 4. 구독자 전용글 체크
        if (post.getVisibility() == PostVisibility.SUBSCRIBERS_ONLY) {
            // 나중에 여기에 실제 구독 여부 확인 로직.
            // boolean isSubscribed = subscriptionRepository.existsBy...(currentUserId, post.getUser().getId());
            // if (isSubscribed) return;

            // 지금은 구독 기능이 없으므로 작성자가 아니면 무조건 예외 발생
            throw new SecurityException("구독자만 열람할 수 있는 게시글입니다.");
        }
    }
}
