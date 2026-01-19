package com.backend.user.service;

import com.backend.user.dto.CreatorResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HomeService {

    Page<CreatorResponseDto> listAllCreators(Pageable pageable);

    Page<CreatorResponseDto> searchCreators(String keyword, Pageable pageable);
}
