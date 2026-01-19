package com.backend.auth.dto;

import com.backend.auth.entity.RefreshToken;
import jakarta.validation.constraints.NotNull;

public record RefreshRequestDto(
        @NotNull
        String refreshToken
) {
}
