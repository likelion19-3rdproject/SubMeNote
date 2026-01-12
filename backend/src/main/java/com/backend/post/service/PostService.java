package com.backend.post.service;

import com.backend.post.dto.PostCreateRequestDto;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.dto.PostUpdateRequestDto;

public interface PostService {
    PostResponseDto create(Long userId, PostCreateRequestDto request);
    PostResponseDto update(Long commentId, Long userId, PostUpdateRequestDto request);
    void delete(Long commentId, Long userId);
}
