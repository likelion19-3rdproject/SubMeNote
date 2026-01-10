package com.backend.subscribe.service;

import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.entity.SubscribeStatus;

public interface SubscribeService {
    SubscribeResponseDto createSubscribe(Long userId, Long creatorId);

    SubscribeResponseDto updateStatus(Long userId, Long subscribeId, SubscribeStatus status);

    void deleteSubscribe(Long userId, Long subscribeId);

}
