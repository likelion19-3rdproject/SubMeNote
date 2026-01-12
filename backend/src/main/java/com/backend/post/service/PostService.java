package com.backend.post.service;

import com.backend.post.dto.PostCreateRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.dto.PostUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    // 게시글 생성
    Long createPost(PostCreateRequestDto request, Long userId);

    // 게시글 수정
    PostResponseDto updatePost(Long postId, PostUpdateRequestDto request, Long userId);

    // 게시글 삭제
    void deletePost(Long postId, Long userId);

    // 게시글 전체 조회 (목록)
    Page<PostResponseDto> getPostList(Pageable pageable);

    // 게시글 단건 조회 (상세)
    PostResponseDto getPost(Long postId, Long currentUserId);

    //특정 크리에이터의 게시글 목록 조회 (구독자만 가능)
    Page<PostResponseDto> getCreatorPostList(Long creatorId, Long currentUserId, Pageable pageable);

    // 내가 작성한 게시글 목록 조회
    Page<PostResponseDto> getMyPostList(Long userId, Pageable pageable);
}