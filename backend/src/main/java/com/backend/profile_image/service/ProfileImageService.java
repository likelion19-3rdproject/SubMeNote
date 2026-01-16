package com.backend.profile_image.service;

import com.backend.profile_image.entity.ProfileImage;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageService {
    void uploadOrReplace(Long userId, MultipartFile file);
    ProfileImage download(Long userId);
}