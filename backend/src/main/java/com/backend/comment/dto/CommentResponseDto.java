package com.backend.comment.dto;


import com.backend.comment.entity.Comment;
import com.backend.comment.entity.CommentReportStatus;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        Long userId,
        String nickname,
        String content,
        CommentReportStatus status,
        Long postId,
        String postTitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getStatus(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
