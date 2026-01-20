package com.backend.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record AnnouncementRequestDto(

        @NotBlank(message = "공지사항은 반드시 입력해야 합니다.")
        String message
) {
}
