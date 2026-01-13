package com.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDto(
        @NotBlank(message = "댓글은 반드시 입력해야 합니다.")
        String content
) {
}
