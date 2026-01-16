package com.backend.report.dto;

import com.backend.post.entity.Post;
import com.backend.post.entity.PostReportStatus;
import com.backend.post.entity.PostVisibility;

import java.time.LocalDateTime;

public record HiddenPostResponseDto(
        Long postId,
        Long userId,
        String nickName,
        String title,
        PostVisibility visibility,
        PostReportStatus status,
        LocalDateTime createdAt
) {
    public static HiddenPostResponseDto from(Post post) {
        return new HiddenPostResponseDto(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getTitle(),
                post.getVisibility(),
                post.getStatus(),
                post.getCreatedAt()
        );
    }
}