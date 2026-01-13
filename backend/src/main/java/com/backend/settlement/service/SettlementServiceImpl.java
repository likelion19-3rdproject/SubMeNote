package com.backend.settlement.service;

import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.role.entity.RoleEnum;
import com.backend.settlement.dto.SettlementResponseDto;
import com.backend.settlement.entity.Settlement;
import com.backend.settlement.repository.SettlementRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    /**
     * 정산 내역 조회
     * <br/>
     * 1. CREATOR 권한 확인
     * 2. 정산 내역 조회
     */
    @Override
    public Page<SettlementResponseDto> getMySettlements(Long creatorId, Pageable pageable) {
        // CREATOR 권한 확인
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        // 정산 내역 조회
        Page<Settlement> settlements
                = settlementRepository.findByCreatorIdOrderByPeriodEndDesc(creatorId, pageable);

        return settlements.map(settlement
                        -> SettlementResponseDto.from(settlement, user.getNickname())
                );
    }
}
