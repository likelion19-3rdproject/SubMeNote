package com.backend.post.dto;

import com.backend.post.entity.Post;
import com.backend.post.entity.PostReportStatus;
import com.backend.post.entity.PostVisibility;

import java.time.LocalDateTime;

public record PostResponseDto(
        Long id,
        Long userId,
        String nickname,
        String title,
        String content,
        PostVisibility visibility,
        PostReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getTitle(),
                post.getContent(),
                post.getVisibility(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
