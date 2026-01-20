package com.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDto(

        @NotBlank(message = "댓글은 반드시 입력해야 합니다.")
        String content,

        //부모 댓글 ID (null이면 최상위 댓글)
        Long parentId
) {
}
