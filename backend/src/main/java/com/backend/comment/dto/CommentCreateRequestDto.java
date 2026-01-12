package com.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDto(
        @NotBlank String content
) {
}
