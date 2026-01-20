package com.backend.global.config;

import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
    }

    /* ================= ROLE ================= */

    private void seedRoles() {
        for (RoleEnum roleEnum : EnumSet.allOf(RoleEnum.class)) {
            roleRepository.findByRole(roleEnum)
                    .orElseGet(() -> roleRepository.save(Role.of(roleEnum)));
        }
    }
}
