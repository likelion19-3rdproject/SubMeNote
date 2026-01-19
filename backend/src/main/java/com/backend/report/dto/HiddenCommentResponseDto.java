package com.backend.report.dto;

import com.backend.comment.entity.Comment;
import com.backend.comment.entity.CommentReportStatus;

import java.time.LocalDateTime;

public record HiddenCommentResponseDto(
        Long commentId,
        Long userId,
        String nickName,
        Long postId,
        String content,
        CommentReportStatus status,
        LocalDateTime createdAt
) {
    public static HiddenCommentResponseDto from(Comment comment) {
        return new HiddenCommentResponseDto(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt()
        );
    }
}