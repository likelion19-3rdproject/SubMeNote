package com.backend.role.repository;

import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {

    Optional<Role> findByRole(RoleEnum role);
}
