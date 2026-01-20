package com.backend;

import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@EnableScheduling
@SpringBootApplication(exclude = {
        BatchAutoConfiguration.class
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    @Profile({"dev", "local"})
    public CommandLineRunner commandLineRunner(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role roleAdmin = roleRepository.save(Role.of(RoleEnum.ROLE_ADMIN));
            Role roleCreator = roleRepository.save(Role.of(RoleEnum.ROLE_CREATOR));
            Role roleUser = roleRepository.save(Role.of(RoleEnum.ROLE_USER));

            roleRepository.saveAll(Set.of(roleAdmin, roleCreator, roleUser));

            String encoded = passwordEncoder.encode("password123!");

            User admin = User.of("admin@email.com", "admin", encoded, Set.of(roleAdmin));

            User creator1 = User.of("creator1@email.com", "creator1", encoded, Set.of(roleCreator));
            User creator2 = User.of("creator2@email.com", "creator2", encoded, Set.of(roleCreator));

            User user1 = User.of("user1@email.com", "user1", encoded, Set.of(roleUser));
            User user2 = User.of("user2@email.com", "user2", encoded, Set.of(roleUser));

            userRepository.saveAll(List.of(admin, creator1, creator2, user1, user2));
        };
    }
}
