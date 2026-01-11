package com.backend.subscribe.dto;

import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;

import java.time.LocalDateTime;

public record SubscribedCreatorResponseDto(
        Long subscriptionId,
        Long creatorId,
        String creatorNickname,
        SubscribeStatus status,
        LocalDateTime expiredAt

) {
    public static SubscribedCreatorResponseDto from(Subscribe subscribe){
        return new SubscribedCreatorResponseDto(
                subscribe.getId(),
                subscribe.getCreator().getId(),
                subscribe.getCreator().getNickname(),
                subscribe.getStatus(),
                subscribe.getExpiredAt()
        );
    }
}
