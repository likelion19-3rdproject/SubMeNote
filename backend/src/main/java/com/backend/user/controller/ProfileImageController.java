package com.backend.user.controller;

import com.backend.profile_image.entity.ProfileImage;
import com.backend.profile_image.service.ProfileImageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile-images")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    /**
     * 특정 유저 프로필 이미지 다운로드(바이너리)
     * - 프론트에서 <img src="/api/profile-images/users/{userId}"> 로 사용 가능
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<byte[]> download(@PathVariable Long userId) {

        ProfileImage img = profileImageService.download(userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + img.getOriginalName() + "\""
                )
                .body(img.getData());
    }
}