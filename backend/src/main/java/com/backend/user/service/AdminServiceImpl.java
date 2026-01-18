package com.backend.user.service;

import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.user.dto.ApplicationProcessRequestDto;
import com.backend.user.dto.CreatorApplicationResponseDto;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.UserResponseDto;
import com.backend.user.entity.ApplicationStatus;
import com.backend.user.entity.CreatorApplication;
import com.backend.user.entity.User;
import com.backend.user.repository.ApplicationRepository;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleRepository roleRepository;

    // 크리에이터 신청 목록 조회
    @Override
    @Transactional
    public Page<CreatorApplicationResponseDto> getPendingApplications(Long adminId, Pageable pageable) {

        isAdmin(adminId);

        Page<CreatorApplication> applications
                = applicationRepository.findAll(pageable);

        return applications.map(CreatorApplicationResponseDto::from);
    }

    // 크리에이터 신청 처리
    @Override
    @Transactional
    public void processApplication(
            Long adminId,
            Long applicationId,
            ApplicationProcessRequestDto requestDto
    ) {
        isAdmin(adminId);

        CreatorApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.APPLICATION_NOT_FOUND));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(UserErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        if (requestDto.approved()) {
            approveApplication(application);
        } else {
            rejectApplication(application);
        }
    }

    // 크리에이터 신청 승인
    private void approveApplication(CreatorApplication application) {
        Long userId = application.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Role creatorRole = roleRepository.findByRole(RoleEnum.ROLE_CREATOR)
                .orElseThrow(() -> new BusinessException(UserErrorCode.ROLE_NOT_FOUND));

        user.updateRole(Set.of(creatorRole));
        application.approve();
        userRepository.save(user);
        applicationRepository.save(application);
    }

    // 크리에이터 신청 거절
    private void rejectApplication(CreatorApplication application) {
        application.reject();
        applicationRepository.save(application);
    }

    // 전체 크리에이터 수 조회
    @Override
    @Transactional(readOnly = true)
    public Long getCreatorCount(Long userId) {

        isAdmin(userId);

        return userRepository.countByRoleEnum(RoleEnum.ROLE_CREATOR);
    }

    // 전체 크리에이터 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<CreatorResponseDto> getCreatorList(Long userId, Pageable pageable) {

        isAdmin(userId);

        Page<User> creators = userRepository.findByRoleEnum(RoleEnum.ROLE_CREATOR, pageable);

        return creators.map(CreatorResponseDto::from);
    }

    // 전체 유저 수 조회
    @Override
    @Transactional(readOnly = true)
    public Long getUserCount(Long userId) {

        isAdmin(userId);

        return userRepository.countByRoleEnum(RoleEnum.ROLE_USER);
    }

    // 전체 유저 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUserList(Long userId, Pageable pageable) {

        isAdmin(userId);

        Page<User> users = userRepository.findByRoleEnum(RoleEnum.ROLE_USER, pageable);

        return users.map(UserResponseDto::from);
    }

    private void isAdmin(Long userId) {
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!admin.hasRole(RoleEnum.ROLE_ADMIN)) {
            throw new BusinessException(UserErrorCode.ADMIN_ONLY);
        }
    }
}
