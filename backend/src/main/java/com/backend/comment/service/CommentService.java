package com.backend.comment.service;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    // 댓글 생성
    CommentResponseDto create(Long postId, Long userId, CommentCreateRequestDto request);

    // 댓글 수정
    CommentResponseDto update(Long commentId, Long userId, CommentUpdateRequestDto request);

    // 댓글 삭제
    void delete(Long commentId, Long userId);

    //댓글 조회 (게시글의 댓글 목록)
    Page<CommentResponseDto> getComments(Long postId, Long currentUserId, Pageable pageable);

    // 내 댓글 조회
    Page<CommentResponseDto> getMyComments(Long userId, Pageable pageable);
}