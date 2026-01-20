package com.backend.role.repository;

import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {

    @Query("SELECT r FROM Role r WHERE r.role = :roleEnum ORDER BY r.id ASC")
    Optional<Role> findFirstByRole(@Param("roleEnum") RoleEnum roleEnum);
    
    // 기존 메서드 호환성을 위한 별칭
    default Optional<Role> findByRole(RoleEnum roleEnum) {
        return findFirstByRole(roleEnum);
    }
}
