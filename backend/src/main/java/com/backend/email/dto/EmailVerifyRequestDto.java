package com.backend.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerifyRequestDto(

        @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
        @Email(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email,

        @NotBlank(message = "인증번호를 입력해주세요.")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "인증번호는 숫자 6자리입니다."

        )
        String authCode
) {
}
