package com.backend.user.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.post.dto.PostResponseDto;
import com.backend.post.service.PostService;
import com.backend.profile_image.service.ProfileImageService;
import com.backend.user.dto.AccountRequestDto;
import com.backend.user.dto.AccountResponseDto;
import com.backend.user.service.CreatorService;
import com.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 크리에이터만 할 수 있는 일
 */
@RestController
@RequestMapping("/api/creator/me")
@RequiredArgsConstructor
public class CreatorController {

    private final CreatorService creatorService;
    private final PostService postService;
    private final ProfileImageService profileImageService;

    /**
     * 내가 작성한 게시글 조회
     * 게시글은 크리에이터만 작성 가능
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Long userId = userDetails.getUserId();

        Page<PostResponseDto> response = postService.getMyPostList(userId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 크리에이터 계좌 등록
     */
    @PostMapping("/account")
    public ResponseEntity<?> registerAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccountRequestDto requestDto
    ) {

        Long userId = userDetails.getUserId();

        creatorService.registerAccount(userId, requestDto);

        return ResponseEntity.ok().build();
    }

    /**
     * 크리에이터 계좌 조회
     */
    @GetMapping("/account")
    public ResponseEntity<AccountResponseDto> getAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        AccountResponseDto responseDto = creatorService.getAccount(userDetails.getUserId());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 크리에이터 계좌 수정
     */
    @PatchMapping("/account")
    public ResponseEntity<?> updateAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccountRequestDto requestDto
    ) {

        Long userId = userDetails.getUserId();

        creatorService.updateAccount(userId, requestDto);

        return ResponseEntity.ok().build();
    }

    /**
     * 내 프로필 이미지 업로드/교체
     */
    @PostMapping(value = "/profile-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMyProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file
    ) {

        profileImageService.uploadOrReplace(userDetails.getUserId(), file);

        return ResponseEntity.ok().build();
    }
}
