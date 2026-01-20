package com.backend.subscribe.service.impl;

import com.backend.global.exception.domain.SubscribeErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.validator.RoleValidator;
import com.backend.subscribe.dto.SubscribedCreatorResponseDto;
import com.backend.subscribe.dto.SubscribeResponseDto;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.subscribe.service.SubscribeService;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final UserRepository userRepository;
    private final RoleValidator roleValidator;

    /**
     * 구독하기
     */
    @Override
    @Transactional
    public SubscribeResponseDto createSubscribe(Long userId, Long creatorId) {

        User subscriber = userRepository.findByIdOrThrow(userId);


        //크리에이터인지 확인
        User creator = roleValidator.validateCreator(creatorId);

        //자기자신 구독 방지
        if (userId.equals(creatorId)) {
            throw new BusinessException(SubscribeErrorCode.CANNOT_SUBSCRIBE_SELF);
        }

        Subscribe subscribe = Subscribe.of(subscriber, creator, SubscribeStatus.ACTIVE, null, SubscribeType.FREE);

        return SubscribeResponseDto.from(subscribeRepository.save(subscribe));
    }

    /**
     * 멤버십 갱신
     */
    @Override
    @Transactional
    public void renewMembership(Long userId, Long creatorId, int months) {

        userRepository.findByIdOrThrow(userId);


        //크리에이터인지 확인
        roleValidator.validateCreator(creatorId);

        //자기자신 구독 방지
        if (userId.equals(creatorId)) {
            throw new BusinessException(SubscribeErrorCode.CANNOT_SUBSCRIBE_SELF);
        }

        LocalDate now = LocalDate.now();

        Subscribe subscribe = subscribeRepository.findByUser_IdAndCreator_Id(userId, creatorId)
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        subscribe.activate();
        LocalDate expiredAt = subscribe.getExpiredAt();

        //이미 멤버쉽 구독중인 경우 기존 만료일에서 연장
        if (expiredAt != null && !expiredAt.isBefore(now)) {
            subscribe.renewMembership(expiredAt.plusMonths(months));
        } else {
            //처음 구독하면 오늘 기준으로 만료일 세팅
            subscribe.renewMembership(now.plusMonths(months));
        }
    }

    /**
     * 구독 상태 변경
     */
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

        switch (status) {
            case CANCELED -> subscribe.cancel();
            case ACTIVE -> subscribe.activate();
        }

        return SubscribeResponseDto.from(subscribe);
    }

    /**
     * 구독 정보 삭제
     */
    @Override
    @Transactional
    public void deleteSubscribe(Long userId, Long subscribeId) {
        // 구독 정보 있는지 체크
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        //구독정보와 로그인한 유저 정보 일치하는지
        if (!subscribe.getUser().getId().equals(userId)) {
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }

        //만료 안되었으면 삭제 불가
        if (subscribe.getExpiredAt() != null && LocalDate.now().isBefore(subscribe.getExpiredAt())) {
            throw new BusinessException(SubscribeErrorCode.CANNOT_DELETE_NOT_EXPIRED);
        }

        subscribeRepository.delete(subscribe);
    }

    /**
     * 구독한 크리에이터 목록 보기
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SubscribedCreatorResponseDto> findSubscribedCreator(Long userId, Pageable pageable) {

        Page<Subscribe> page = subscribeRepository.findByUser_Id(userId, pageable);

        return page.map(SubscribedCreatorResponseDto::from);
    }
}
