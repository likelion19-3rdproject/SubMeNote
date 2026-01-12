package com.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailCodeRequestDto(
        @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
        @Email(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email
) {
}
