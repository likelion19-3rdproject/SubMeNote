package com.backend.post.service;

import com.backend.post.dto.PostCreateRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.dto.PostUpdateRequestDto;
import com.backend.post.entity.Post;
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

    //게시글 단건 조회 (상세)
    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

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
}
