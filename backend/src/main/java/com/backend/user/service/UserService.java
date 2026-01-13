package com.backend.user.service;

import com.backend.user.dto.CreatorResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<CreatorResponseDto> listAllCreators(int page, int size);

    void signout(Long userId);
}
