package com.backend.user.service;

import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.user.dto.ApplicationProcessRequestDto;
import com.backend.user.dto.CreatorApplicationResponseDto;
import com.backend.user.entity.ApplicationStatus;
import com.backend.user.entity.CreatorApplication;
import com.backend.user.entity.User;
import com.backend.user.repository.ApplicationRepository;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Page<CreatorApplicationResponseDto> getPendingApplications(Long adminId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "appliedAt")
        );

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!admin.hasRole(RoleEnum.ROLE_ADMIN)) {
            throw new BusinessException(UserErrorCode.ADMIN_ONLY);
        }

        Page<CreatorApplication> applications
                = applicationRepository.findAll(pageable);

        return applications.map(CreatorApplicationResponseDto::from);
    }

    @Override
    @Transactional
    public void processApplication(Long adminId, Long applicationId, ApplicationProcessRequestDto requestDto) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!admin.hasRole(RoleEnum.ROLE_ADMIN)) {
            throw new BusinessException(UserErrorCode.ADMIN_ONLY);
        }

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

    private void rejectApplication(CreatorApplication application) {
        application.reject();
        applicationRepository.save(application);
    }
}
