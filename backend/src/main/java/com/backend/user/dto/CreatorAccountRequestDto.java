package com.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatorAccountRequestDto(
        @NotBlank(message = "은행 이름은 반드시 입력해야 합니다.")
        String bankName,

        @NotBlank(message = "계좌번호는 반드시 입력해야 합니다.")
        @Pattern(
                regexp = "^[0-9]+(-[0-9]+)*$",
                message = "계좌번호는 숫자와 '-'만 입력할 수 있습니다."
        )
        String accountNumber,

        @NotBlank(message = "예금주는 반드시 입력해야 합니다.")
        String holderName
) {
}
