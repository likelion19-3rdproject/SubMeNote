package com.backend.user.repository;

import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    // CREATOR 역할을 가진 사용자 조회 (페이징)
    @Query("select u from User u JOIN u.role r WHERE r.role = :roleEnum")
    Page<User> findByRoleEnum(RoleEnum roleEnum, Pageable pageable);
}
