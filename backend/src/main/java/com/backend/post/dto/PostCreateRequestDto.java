package com.backend.post.dto;

import com.backend.post.entity.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record PostCreateRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull PostVisibility visibility
) {
}
