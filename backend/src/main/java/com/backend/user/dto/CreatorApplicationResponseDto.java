package com.backend.user.dto;

import com.backend.user.entity.ApplicationStatus;
import com.backend.user.entity.CreatorApplication;

import java.time.LocalDateTime;

public record CreatorApplicationResponseDto(
        String nickname,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {

    public static CreatorApplicationResponseDto from(CreatorApplication creatorApplication) {
        return new CreatorApplicationResponseDto(
                creatorApplication.getUser().getNickname(),
                creatorApplication.getStatus(),
                creatorApplication.getAppliedAt()
        );
    }
}
