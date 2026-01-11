package com.backend.subscribe.service;

import com.backend.subscribe.dto.SubscribedCreatorResponseDto;
import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscribeService {
    SubscribeResponseDto createSubscribe(Long userId, Long creatorId , SubscribeType type);

    SubscribeResponseDto updateStatus(Long userId, Long subscribeId, SubscribeStatus status);

    void deleteSubscribe(Long userId, Long subscribeId);

    Page<SubscribedCreatorResponseDto> findSubscribedCreator(Long userId, Pageable pageable);

}
