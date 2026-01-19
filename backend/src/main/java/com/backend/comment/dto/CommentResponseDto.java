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
        Long parentId,
        List<CommentResponseDto> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long likeCount,
        boolean likedByMe
) {
    public static CommentResponseDto from(Comment comment, Long postId) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getStatus(),
                postId, //외부에서 주입받기
                comment.getParent() != null ? comment.getParent().getId() : null, // 부모 ID 매핑
                List.of(), //재귀 금지 (무조건 비움)
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                0L,
                false
        );
    }
}
