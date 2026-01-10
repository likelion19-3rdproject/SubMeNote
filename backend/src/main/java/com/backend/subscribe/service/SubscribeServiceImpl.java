package com.backend.subscribe.service;

import com.backend.global.exception.SubscribeErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {
    private final SubscribeRepository subscribeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SubscribeResponseDto createSubscribe(Long userId, Long creatorId) {
        User subscriber = userRepository.findById(userId)
                //UserErrorCode 구현시 변경
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        User creator = userRepository.findById(creatorId)
                //UserErrorCode 구현시 변경
                .orElseThrow(()-> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        if (userId.equals(creatorId)) {
            throw new BusinessException(SubscribeErrorCode.CANNOT_SUBSCRIBE_SELF);
        }

        LocalDateTime now = LocalDateTime.now();

        //만료되고 삭제되기 전에
        Subscribe subscribe = subscribeRepository.findByUser_IdAndCreator_Id(userId, creatorId)
                .map(existing -> {
                    if (existing.getExpiredAt().isAfter(now)) {
                        throw new BusinessException(SubscribeErrorCode.ALREADY_SUBSCRIBED);
                    }
                    existing.renew(now);
                    return existing;
                })
                .orElseGet(() -> new Subscribe(
                        subscriber, creator, SubscribeStatus.ACTIVE, now.plusMonths(1)
                ));

        return SubscribeResponseDto.from(subscribeRepository.save(subscribe));
    }

    @Override
    @Transactional
    public SubscribeResponseDto updateStatus(Long userId, Long subscribeId, SubscribeStatus status) {
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        if (!subscribe.getUser().getId().equals(userId)) {
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }

        switch(status){
            case CANCELED -> subscribe.cancel();
            case ACTIVE -> subscribe.activate();
        }
        return SubscribeResponseDto.from(subscribe);
    }

    @Override
    @Transactional
    public void deleteSubscribe(Long userId, Long subscribeId) {
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(()-> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));
        if(!subscribe.getUser().getId().equals(userId)){
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }
        if(LocalDateTime.now().isBefore(subscribe.getExpiredAt())){
            throw new BusinessException(SubscribeErrorCode.CANNOT_DELETE_NOT_EXPIRED);
        }
        subscribeRepository.delete(subscribe);
    }
}
