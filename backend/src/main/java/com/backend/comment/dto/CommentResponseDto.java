package com.backend.comment.dto;


import com.backend.comment.entity.Comment;
import com.backend.comment.entity.CommentReportStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponseDto(
        Long id,
        Long userId,
        String nickname,
        String content,
        CommentReportStatus status,
        Long postId,
        String postTitle,
        Long parentId,
        List<CommentResponseDto> children,
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
                comment.getParent() != null ? comment.getParent().getId() : null, // 부모 ID 매핑
                comment.getChildren().stream()
                        .map(CommentResponseDto::from)
                        .toList(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
