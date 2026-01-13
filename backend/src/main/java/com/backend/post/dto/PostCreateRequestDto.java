package com.backend.post.dto;

import com.backend.post.entity.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record PostCreateRequestDto(
        @NotBlank(message = "제목은 반드시 입력해야 합니다.")
        String title,

        @NotBlank(message = "제목은 반드시 입력해야 합니다.")
        String content,

        @NotNull(message = "공개 범위를 설정해야 합니다.")
        PostVisibility visibility
) {
}
