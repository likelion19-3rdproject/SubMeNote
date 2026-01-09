package com.backend.post.dto;

import com.backend.post.entity.PostVisibility;
import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequestDto(
       @NotBlank String title,
       @NotBlank String content,
       @NotBlank PostVisibility visibility
) {

}
