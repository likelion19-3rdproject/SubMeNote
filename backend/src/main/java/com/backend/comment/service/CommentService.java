package com.backend.comment.service;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponseDto create(Long postId, Long userId, CommentCreateRequestDto request);
    CommentResponseDto update(Long commentId, Long userId, CommentUpdateRequestDto request);
    void delete(Long commentId, Long userId);
    Page<CommentResponseDto> getComments(Long postId, Pageable pageable);
    Page<CommentResponseDto> getMyComments(Long userId, Pageable pageable);
}