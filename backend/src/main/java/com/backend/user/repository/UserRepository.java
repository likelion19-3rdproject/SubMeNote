package com.backend.user.repository;

import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    // CREATOR 역할을 가진 사용자 조회 (페이징)
    @Query("select u from User u JOIN u.role r WHERE r.role = :roleEnum")
    Page<User> findByRoleEnum(RoleEnum roleEnum, Pageable pageable);


    // 배치,스케줄러용
    @Query("select distinct u from User u join u.role r where r.role = :roleEnum")
    List<User> findAllByRoleEnum(RoleEnum roleEnum);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);

    @Query("select count(distinct u) from User u " +
            "join u.role r" +
            " where r.role = :roleEnum")
    Long countByRoleEnum(RoleEnum roleEnum);
}
