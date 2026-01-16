package com.backend.auth.dto;

import com.backend.role.entity.RoleEnum;
import jakarta.validation.constraints.*;

public record SignupRequestDto(
        @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
        @Email(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email,

        @NotBlank(message = "닉네임은 반드시 입력해야 합니다.")
        @Pattern(
                regexp = "^\\S{2,}$",
                message = "공백 없이 2자 이상 입력해 주세요."
        )
        String nickname,

        @NotBlank(message = "비밀번호는 반드시 입력해야 합니다.")
        @Size(
                min = 8,
                max = 16,
                message = "비밀번호는 8자 이상 16자 이하로 입력해주세요."
        )
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
        )
        String password
) {
}
