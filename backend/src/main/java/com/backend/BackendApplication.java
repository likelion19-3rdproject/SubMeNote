package com.backend;

import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
//@EnableJpaAuditing
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role adminRole = roleRepository.save(new Role(RoleEnum.ROLE_ADMIN));
            Role creatorRole = roleRepository.save(new Role(RoleEnum.ROLE_CREATOR));
            Role userRole = roleRepository.save(new Role(RoleEnum.ROLE_USER));

            String encode = passwordEncoder.encode("password123!");

            User user1 = new User("user1@email.com", "user1", encode, Set.of(userRole));
            User user2 = new User("user2@email.com", "user2", encode, Set.of(userRole));
            User user3 = new User("user3@email.com", "user3", encode, Set.of(userRole));

            User creator1 = new User("creator1@email.com", "creator1", encode, Set.of(creatorRole));
            User creator2 = new User("creator2@email.com", "creator2", encode, Set.of(creatorRole));
            User creator3 = new User("creator3@email.com", "creator3", encode, Set.of(creatorRole));

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
            userRepository.save(creator1);
            userRepository.save(creator2);
            userRepository.save(creator3);
        };
    }
}
