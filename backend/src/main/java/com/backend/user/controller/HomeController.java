package com.backend.user.controller;

import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 전체 크리에이터 조회 (홈화면)
     */
    @GetMapping
    public ResponseEntity<Page<CreatorResponseDto>> home(
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<CreatorResponseDto> creators = homeService.listAllCreators(pageable);

        return ResponseEntity.ok(creators);
    }

    /**
     * 홈화면에서 크리에이터 검색
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CreatorResponseDto>> searchCreators(
            @RequestParam String keyword,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<CreatorResponseDto> creators = homeService.searchCreators(keyword, pageable);

        return ResponseEntity.ok(creators);
    }
}
