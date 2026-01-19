package com.backend.user.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.profile_image.entity.ProfileImage;
import com.backend.profile_image.service.ProfileImageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile-images")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

//    /**
//     * 내 프로필 이미지 업로드/교체 (CREATOR만)
//     */
//    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> uploadMyProfileImage(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestPart("file") MultipartFile file
//    ) {
//        profileImageService.uploadOrReplace(userDetails.getUserId(), file);
//        return ResponseEntity.ok().build();
//    }

    /**
     * 특정 유저 프로필 이미지 다운로드(바이너리)
     * - 프론트에서 <img src="/api/profile-images/users/{userId}"> 로 사용 가능
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<byte[]> download(@PathVariable Long userId) {

        ProfileImage img = profileImageService.download(userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + img.getOriginalName() + "\"")
                .body(img.getData());
    }
}