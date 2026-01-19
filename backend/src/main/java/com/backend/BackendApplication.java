package com.backend;

import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.post.repository.PostRepository;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    @Transactional // DB 작업이므로 트랜잭션 안에서 실행 추천
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  PostRepository postRepository,
                                  PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. 역할(Role) 초기화: 이미 있으면 가져오고, 없으면 생성
            Role adminRole = roleRepository.findByRole(RoleEnum.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(Role.of(RoleEnum.ROLE_ADMIN)));

            Role creatorRole = roleRepository.findByRole(RoleEnum.ROLE_CREATOR)
                    .orElseGet(() -> roleRepository.save(Role.of(RoleEnum.ROLE_CREATOR)));

            Role userRole = roleRepository.findByRole(RoleEnum.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(Role.of(RoleEnum.ROLE_USER)));

            String password = passwordEncoder.encode("password123!");

            // 2. 유저 초기화: 이메일로 존재 여부 확인 후 없을 때만 생성
            if (!userRepository.existsByEmail("admin@email.com")) {
                User admin = User.of("admin@email.com", "admin", password, Set.of(adminRole));
                userRepository.save(admin);
            }

            if (!userRepository.existsByEmail("user1@email.com")) {
                User user1 = User.of("user1@email.com", "user1", password, Set.of(userRole));
                User user2 = User.of("user2@email.com", "user2", password, Set.of(userRole));
                User user3 = User.of("user3@email.com", "user3", password, Set.of(userRole));
                userRepository.saveAll(List.of(user1, user2, user3));
            }

            if (!userRepository.existsByEmail("creator1@email.com")) {
                User creator1 = User.of("creator1@email.com", "creator1", password, Set.of(creatorRole));
                User creator2 = User.of("creator2@email.com", "creator2", password, Set.of(creatorRole));
                User creator3 = User.of("creator3@email.com", "creator3", password, Set.of(creatorRole));
                userRepository.saveAll(List.of(creator1, creator2, creator3));
            }

            // 3. 게시글 테스트 데이터 초기화
            Optional<User> creator1Opt = userRepository.findByEmail("creator1@email.com");
            Optional<User> creator2Opt = userRepository.findByEmail("creator2@email.com");
            
            if (creator1Opt.isPresent() && creator2Opt.isPresent()) {
                User creator1 = creator1Opt.get();
                User creator2 = creator2Opt.get();

                // creator1의 게시글 생성
                if (postRepository.findAllByUserId(creator1.getId(), org.springframework.data.domain.PageRequest.of(0, 1)).isEmpty()) {
                    Post post1 = Post.create("테스트 게시글 제목 1", "이것은 테스트 게시글 내용입니다.", PostVisibility.PUBLIC, creator1);
                    Post post2 = Post.create("검색 테스트 제목", "검색 기능 테스트를 위한 게시글입니다.", PostVisibility.PUBLIC, creator1);
                    Post post3 = Post.create("구독자 전용 게시글", "구독자만 볼 수 있는 게시글입니다.", PostVisibility.SUBSCRIBERS_ONLY, creator1);
                    postRepository.saveAll(List.of(post1, post2, post3));
                }

                // creator2의 게시글 생성
                if (postRepository.findAllByUserId(creator2.getId(), org.springframework.data.domain.PageRequest.of(0, 1)).isEmpty()) {
                    Post post4 = Post.create("크리에이터 2의 첫 게시글", "크리에이터 2가 작성한 게시글입니다.", PostVisibility.PUBLIC, creator2);
                    Post post5 = Post.create("제목에 검색이 포함된 글", "이 글의 제목에는 검색이라는 키워드가 포함되어 있습니다.", PostVisibility.PUBLIC, creator2);
                    postRepository.saveAll(List.of(post4, post5));
                }
            }
        };
    }
}
