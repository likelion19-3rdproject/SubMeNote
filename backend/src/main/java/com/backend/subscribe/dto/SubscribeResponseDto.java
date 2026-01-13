package com.backend.subscribe.dto;

import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;

import java.time.LocalDateTime;

public record SubscribeResponseDto(
        Long id,
        Long creatorId,
        SubscribeStatus status,
        SubscribeType type,
        LocalDateTime expiredAt
) {
    public static SubscribeResponseDto from(Subscribe subscribe){
        return new SubscribeResponseDto(
                subscribe.getId(),
                subscribe.getCreator().getId(),
                subscribe.getStatus(),
                subscribe.getType(),
                subscribe.getExpiredAt()
        );
    }
}