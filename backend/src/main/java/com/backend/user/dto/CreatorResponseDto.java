package com.backend.user.dto;

import com.backend.user.entity.User;

public record CreatorResponseDto(
        Long creatorId,
        String nickname
) {
    public static CreatorResponseDto from(User creator) {
        return new CreatorResponseDto(
                creator.getId(),
                creator.getNickname()
        );
    }
}
