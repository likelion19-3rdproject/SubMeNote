package com.backend.post.service;

import com.backend.post.dto.PostRequestDto;
import com.backend.post.dto.PostResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponseDto create(Long userId, PostRequestDto request);

    PostResponseDto update(Long postId, Long userId, PostRequestDto request);

    void delete(Long postId, Long userId);

    // 게시글 전체 조회 (목록)
    Page<PostResponseDto> getPostList(Long currentUserId, Pageable pageable);

    // 구독한 크리에이터들의 게시글 검색
    Page<PostResponseDto> searchSubscribedPosts(Long currentUserId, String keyword, Pageable pageable);

    // 게시글 단건 조회 (상세)
    PostResponseDto getPost(Long postId, Long currentUserId);

    //특정 크리에이터의 게시글 목록 조회 (구독자만 가능)
    Page<PostResponseDto> getCreatorPostList(Long creatorId, Long currentUserId, Pageable pageable);

    // 내가 작성한 게시글 목록 조회
    Page<PostResponseDto> getMyPostList(Long userId, Pageable pageable);
}
