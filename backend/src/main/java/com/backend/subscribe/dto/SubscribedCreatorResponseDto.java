package com.backend.subscribe.dto;

import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;

import java.time.LocalDate;

public record SubscribedCreatorResponseDto(
        Long subscriptionId,
        Long creatorId,
        String creatorNickname,
        SubscribeStatus status,
        SubscribeType type,
        LocalDate expiredAt

) {
    public static SubscribedCreatorResponseDto from(Subscribe subscribe){
        return new SubscribedCreatorResponseDto(
                subscribe.getId(),
                subscribe.getCreator().getId(),
                subscribe.getCreator().getNickname(),
                subscribe.getStatus(),
                subscribe.getType(),
                subscribe.getExpiredAt()
        );
    }
}
