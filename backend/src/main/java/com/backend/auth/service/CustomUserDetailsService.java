package com.backend.auth.service;

import com.backend.global.CustomUserDetails;
import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CustomUserDetails loadByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found: " + userId));

        // user.getRole()은 Set<Role>
        List<RoleEnum> roles = user.getRole().stream()
                .map(r -> r.getRole()) // 변경된 부분에는 role에 getter있음
                .toList();

        if (roles.isEmpty()) {
            // 역할이 없는 사용자는 비정상으로 보고 막는 게 안전
            throw new UsernameNotFoundException("user has no roles: " + userId);
        }

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                roles,
                true // enabled 필드 있으면 여기서 반영
        );
    }
}