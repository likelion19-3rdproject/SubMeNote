package com.backend.subscribe.service;

import com.backend.subscribe.dto.SubscribedCreatorResponseDto;
import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface SubscribeService {
    SubscribeResponseDto createSubscribe(Long userId, Long creatorId);


    @Transactional
    void renewMembership(Long userId, Long creatorId, int months);

    SubscribeResponseDto updateStatus(Long userId, Long subscribeId, SubscribeStatus status);

    void deleteSubscribe(Long userId, Long subscribeId);

    Page<SubscribedCreatorResponseDto> findSubscribedCreator(Long userId, Pageable pageable);

    Subscribe validateSubscription(Long creatorId, Long userId);
}
