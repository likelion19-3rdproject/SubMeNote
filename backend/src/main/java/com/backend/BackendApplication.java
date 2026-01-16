package com.backend;

import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    @Transactional // DB 작업이므로 트랜잭션 안에서 실행 추천
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. 역할(Role) 초기화: 이미 있으면 가져오고, 없으면 생성
            Role adminRole = roleRepository.findByRole(RoleEnum.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleEnum.ROLE_ADMIN)));

            Role creatorRole = roleRepository.findByRole(RoleEnum.ROLE_CREATOR)
                    .orElseGet(() -> roleRepository.save(new Role(RoleEnum.ROLE_CREATOR)));

            Role userRole = roleRepository.findByRole(RoleEnum.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(RoleEnum.ROLE_USER)));

            String password = passwordEncoder.encode("password123!");

            // 2. 유저 초기화: 이메일로 존재 여부 확인 후 없을 때만 생성
            if (!userRepository.existsByEmail("user1@email.com")) {
                User user1 = new User("user1@email.com", "user1", password, Set.of(userRole));
                User user2 = new User("user2@email.com", "user2", password, Set.of(userRole));
                User user3 = new User("user3@email.com", "user3", password, Set.of(userRole));
                userRepository.saveAll(List.of(user1, user2, user3));
            }

            if (!userRepository.existsByEmail("creator1@email.com")) {
                User creator1 = new User("creator1@email.com", "creator1", password, Set.of(creatorRole));
                User creator2 = new User("creator2@email.com", "creator2", password, Set.of(creatorRole));
                User creator3 = new User("creator3@email.com", "creator3", password, Set.of(creatorRole));
                userRepository.saveAll(List.of(creator1, creator2, creator3));
            }
        };
    }
}