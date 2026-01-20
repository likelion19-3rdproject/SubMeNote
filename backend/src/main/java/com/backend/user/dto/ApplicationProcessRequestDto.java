package com.backend.user.dto;

import jakarta.validation.constraints.NotNull;

public record ApplicationProcessRequestDto(

        @NotNull(message = "승인 여부는 필수입니다.")
        Boolean approved
) {
}
