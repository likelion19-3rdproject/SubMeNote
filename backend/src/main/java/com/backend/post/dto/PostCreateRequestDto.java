package com.backend.post.dto;

import com.backend.post.entity.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public record PostCreateRequestDto(
        @NotNull Long userId,
        @NotBlank String title,
        @NotBlank String content,
        @NotNull PostVisibility visibility
) {
}
