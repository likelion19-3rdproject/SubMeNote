package com.backend.user.service;

import com.backend.role.entity.RoleEnum;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final UserRepository userRepository;

    /**
     * CREATOR 목록 표시 (홈화면)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CreatorResponseDto> listAllCreators(Pageable pageable) {

        Page<User> creators = userRepository.findByRoleEnum(RoleEnum.ROLE_CREATOR, pageable);

        return creators.map(creator -> new CreatorResponseDto(creator.getId(), creator.getNickname()));
    }

    /**
     * 홈화면에서 크리에이터 검색
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CreatorResponseDto> searchCreators(String keyword, Pageable pageable) {

        Page<User> creators = userRepository.findByRoleEnumAndNicknameContaining(
                RoleEnum.ROLE_CREATOR,
                keyword,
                pageable
        );

        return creators.map(creator -> new CreatorResponseDto(creator.getId(), creator.getNickname()));
    }
}
