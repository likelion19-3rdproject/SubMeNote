package com.backend.subscribe.service;

import com.backend.global.exception.SubscribeErrorCode;
import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.subscribe.dto.SubscribedCreatorResponseDto;
import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public SubscribeResponseDto createSubscribe(Long userId, Long creatorId , SubscribeType type) {
        User subscriber = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(()-> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        //크리에이터인지 확인
        if(!creator.hasRole("CREATOR")){
            throw new BusinessException(SubscribeErrorCode.NOT_CREATOR);
        }
        //자기자신 구독 방지
        if (userId.equals(creatorId)) {
            throw new BusinessException(SubscribeErrorCode.CANNOT_SUBSCRIBE_SELF);
        }

        LocalDateTime now = LocalDateTime.now();

        Subscribe subscribe = subscribeRepository.findByUser_IdAndCreator_Id(userId, creatorId)
                .map(existing -> {
                    //구독 정보는 있지만 만료상태면 업데이트
                    if (existing.getExpiredAt().isAfter(now)) {
                        throw new BusinessException(SubscribeErrorCode.ALREADY_SUBSCRIBED);
                    }
                    existing.renew(now,type);
                    return existing;
                })
                //구독 정보가 없으면
                .orElseGet(() -> new Subscribe(
                        subscriber, creator, SubscribeStatus.ACTIVE, now.plusMonths(1),type
                ));

        return SubscribeResponseDto.from(subscribeRepository.save(subscribe));
    }

    @Override
    @Transactional
    public SubscribeResponseDto updateStatus(Long userId, Long subscribeId, SubscribeStatus status) {
        // 구독 정보 있는지 체크
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        //구독정보와 로그인한 유저 정보 일치하는지
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
        // 구독 정보 있는지 체크
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(()-> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));
        //구독정보와 로그인한 유저 정보 일치하는지
        if(!subscribe.getUser().getId().equals(userId)){
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }
        //만료 안되었으면 삭제 불가
        if(LocalDateTime.now().isBefore(subscribe.getExpiredAt())){
            throw new BusinessException(SubscribeErrorCode.CANNOT_DELETE_NOT_EXPIRED);
        }
        subscribeRepository.delete(subscribe);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscribedCreatorResponseDto> findSubscribedCreator(Long userId, Pageable pageable) {
        Page<Subscribe> page = subscribeRepository.findByUser_Id(userId, pageable);
        return page.map(SubscribedCreatorResponseDto::from);
    }


    /**
     * 구독자인지 검증하는 메소드
     * @param userId
     * @param creatorId
     * - 구독자가 아니라면 403 에러 발생
     */
    public void validateSubscribe (Long userId, Long creatorId){
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));
        //크리에이터인지 확인
        if(!creator.hasRole("CREATOR")){
            throw new BusinessException(SubscribeErrorCode.NOT_CREATOR);
        }
        //구독정보 확인
        Subscribe subscribe = subscribeRepository.findByUser_IdAndCreator_Id(userId,creatorId)
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE));
        //구독정보는 있지만 만료된 경우
        if (!subscribe.getExpiredAt().isAfter(LocalDateTime.now())){
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }
    }
}
