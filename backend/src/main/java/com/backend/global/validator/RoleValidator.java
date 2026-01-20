package com.backend.global.validator;

import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.domain.UserErrorCode;
import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleValidator {

    private final UserRepository userRepository;

    /**
     * 크리에이터 권한 검증
     */
    public User validateCreator(Long userId) {

        User creator = userRepository.findByIdOrThrow(userId);

        if (!creator.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        return creator;
    }

    /**
     * 관리자 권한 검증
     */
    public void validateAdmin(Long userId) {

        User admin = userRepository.findByIdOrThrow(userId);

        if (!admin.hasRole(RoleEnum.ROLE_ADMIN)) {
            throw new BusinessException(UserErrorCode.ADMIN_ONLY);
        }
    }
}
