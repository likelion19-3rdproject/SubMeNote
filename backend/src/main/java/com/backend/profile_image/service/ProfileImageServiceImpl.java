package com.backend.profile_image.service;

import com.backend.global.exception.domain.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.profile_image.entity.ProfileImage;
import com.backend.profile_image.repository.ProfileImageRepository;
import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;

import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileImageServiceImpl implements ProfileImageService {

    private static final long MAX_SIZE = 10L * 1024 * 1024;

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;

    @Override
    @Transactional
    public void uploadOrReplace(Long userId, MultipartFile file) {

        User user = userRepository.findByIdOrThrow(userId);

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        validateFile(file);

        // 교체: 기존 이미지 있으면 삭제 후 저장 (unique 충돌 방지)
        profileImageRepository.findByUser_Id(userId).ifPresent(existing -> {
            profileImageRepository.delete(existing);
            profileImageRepository.flush();
        });

        try {
            ProfileImage profileImage = new ProfileImage(
                    user,
                    safeOriginalName(file.getOriginalFilename()),
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes()
            );
            profileImageRepository.save(profileImage);

        } catch (IOException e) {
            throw new BusinessException(UserErrorCode.PROFILE_IMAGE_SAVE_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileImage download(Long userId) {

        return profileImageRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.PROFILE_IMAGE_NOT_FOUND));
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(UserErrorCode.PROFILE_IMAGE_INVALID_TYPE);
        }

        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(UserErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean contentTypeOk = contentType != null && contentType.toLowerCase().startsWith("image/");
        boolean extOk = filename != null && filename.matches("(?i).+\\.(png|jpg|jpeg|gif|webp)$");

        if (!contentTypeOk && !extOk) {
            throw new BusinessException(UserErrorCode.PROFILE_IMAGE_INVALID_TYPE);
        }
    }

    private String safeOriginalName(String originalName) {

        if (originalName == null || originalName.isBlank()) return "profile";

        return originalName.length() > 255 ? originalName.substring(0, 255) : originalName;
    }
}