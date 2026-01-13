package com.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatorAccountRequestDto(
        @NotBlank(message = "은행 이름은 반드시 입력해야 합니다.")
        String bankName,

        @NotBlank(message = "계좌번호는 반드시 입력해야 합니다.")
        String accountNumber,

        @NotBlank(message = "예금주는 반드시 입력해야 합니다.")
        String holderName
) {
}
