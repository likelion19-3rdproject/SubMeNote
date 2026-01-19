package com.backend.subscribe.service;

import com.backend.global.exception.domain.SubscribeErrorCode;
import com.backend.global.exception.domain.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.role.entity.RoleEnum;
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

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {
    private final SubscribeRepository subscribeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SubscribeResponseDto createSubscribe(Long userId, Long creatorId) {

        User subscriber = userRepository.findByIdOrThrow(userId);

        User creator = userRepository.findByIdOrThrow(creatorId);

        //크리에이터인지 확인
        if(!creator.hasRole(RoleEnum.ROLE_CREATOR)){
            throw new BusinessException(SubscribeErrorCode.NOT_CREATOR);
        }

        //자기자신 구독 방지
        if (userId.equals(creatorId)) {
            throw new BusinessException(SubscribeErrorCode.CANNOT_SUBSCRIBE_SELF);
        }
        Subscribe subscribe = new Subscribe(subscriber, creator, SubscribeStatus.ACTIVE, null, SubscribeType.FREE);

        return SubscribeResponseDto.from(subscribeRepository.save(subscribe));
    }

    @Override
    @Transactional
    public void renewMembership(Long userId, Long creatorId, int months){

        userRepository.findByIdOrThrow(userId);

        User creator = userRepository.findByIdOrThrow(creatorId);

        //크리에이터인지 확인
        if(!creator.hasRole(RoleEnum.ROLE_CREATOR)){
            throw new BusinessException(SubscribeErrorCode.NOT_CREATOR);
        }

        //자기자신 구독 방지
        if (userId.equals(creatorId)){
            throw new BusinessException(SubscribeErrorCode.CANNOT_SUBSCRIBE_SELF);
        }

        LocalDate now = LocalDate.now();

        Subscribe subscribe = subscribeRepository.findByUser_IdAndCreator_Id(userId, creatorId)
                .orElseThrow(()->new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        subscribe.activate();
        LocalDate expiredAt = subscribe.getExpiredAt();
        //이미 멤버쉽 구독중인 경우 기존 만료일에서 연장
        if (expiredAt != null && !expiredAt.isBefore(now)) {
            subscribe.renewMembership(expiredAt.plusMonths(months));
        }
        else {
            //처음 구독하면 오늘 기준으로 만료일 세팅
            subscribe.renewMembership(now.plusMonths(months));
        }

    }



    @Override
    @Transactional
    public SubscribeResponseDto updateStatus(Long userId, Long subscribeId, SubscribeStatus status){
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
        if(subscribe.getExpiredAt()!=null&&LocalDate.now().isBefore(subscribe.getExpiredAt())){
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
    @Override
    @Transactional(readOnly = true)
    public Subscribe validateSubscription(Long creatorId, Long userId) {
        // 1. 구독 정보 조회 (없으면 예외)
        Subscribe subscribe = subscribeRepository.findByUser_IdAndCreator_Id(userId, creatorId)
                .orElseThrow(() -> new BusinessException(SubscribeErrorCode.NOT_FOUND_SUBSCRIBE));

        // 2. 유료 구독자 만료 체크
        // 만료일이 존재하고(null 아님), 현재 날짜보다 이전이면(isBefore) -> 만료됨
        if (subscribe.getExpiredAt() != null && subscribe.getExpiredAt().isBefore(LocalDate.now())) {
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }

        // 3. 무료 구독자(또는 날짜 없는 구독) 취소 체크
        // 만료일이 없는데(null), 상태가 취소(CANCELED)라면 -> 접근 불가
        if (subscribe.getExpiredAt() == null && subscribe.getStatus() == SubscribeStatus.CANCELED) {
            throw new BusinessException(SubscribeErrorCode.FORBIDDEN_SUBSCRIBE);
        }

        return subscribe;
    }
}
