package com.backend.comment.service;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;

import java.util.List;

public interface CommentService {
    CommentResponseDto create(Long postId, Long userId, CommentCreateRequestDto request);
    CommentResponseDto update(Long commentId, Long userId, CommentUpdateRequestDto request);
    void delete(Long commentId, Long userId);
    List<CommentResponseDto> getComments(Long postId);
    List<CommentResponseDto> getMyComments(Long userId);
}