/*
package com.backend.global.config;

import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.post.repository.PostRepository;
import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;d

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            log.info("DataInitializer 시작");
            
            // 이미 데이터가 있으면 초기화하지 않음
            if (userRepository.count() > 0) {
                log.info("이미 데이터가 존재하므로 초기화를 건너뜁니다.");
                return;
            }

            log.info("Role 생성 시작");
            // Role 생성
            Role adminRole = roleRepository.save(new Role(RoleEnum.ROLE_ADMIN));
            log.info("Admin Role 생성 완료: {}", adminRole.getId());

            Role creatorRole = roleRepository.save(new Role(RoleEnum.ROLE_CREATOR));
            log.info("Creator Role 생성 완료: {}", creatorRole.getId());

            Role userRole = roleRepository.save(new Role(RoleEnum.ROLE_USER));
            log.info("User Role 생성 완료: {}", userRole.getId());

            log.info("User 생성 시작");
            // User 생성
            User admin = createUser("admin@test.com", "admin", "admin123", Set.of(adminRole));
            log.info("Admin User 생성 완료: {}", admin.getId());
            
            User creator1 = createUser("creator1@test.com", "creator1", "password123", Set.of(creatorRole));
            log.info("Creator1 User 생성 완료: {}", creator1.getId());
            
            User creator2 = createUser("creator2@test.com", "creator2", "password123", Set.of(creatorRole));
            log.info("Creator2 User 생성 완료: {}", creator2.getId());
            
            User user1 = createUser("user1@test.com", "user1", "password123", Set.of(userRole));
            log.info("User1 생성 완료: {}", user1.getId());
            
            User user2 = createUser("user2@test.com", "user2", "password123", Set.of(userRole));
            log.info("User2 생성 완료: {}", user2.getId());

            log.info("Post 생성 시작");
            // Post 생성
            createPost(creator1, "첫 번째 게시글", "이것은 첫 번째 테스트 게시글입니다. 공개 게시글로 설정되어 있습니다.", PostVisibility.PUBLIC);
            createPost(creator1, "구독자 전용 게시글", "이것은 구독자만 볼 수 있는 게시글입니다.", PostVisibility.SUBSCRIBERS_ONLY);
            createPost(creator2, "두 번째 크리에이터의 게시글", "두 번째 크리에이터가 작성한 공개 게시글입니다.", PostVisibility.PUBLIC);
            createPost(creator2, "일상 공유", "오늘의 일상을 공유합니다. 좋은 하루 보내세요!", PostVisibility.PUBLIC);
            createPost(creator1, "기술 블로그", "Spring Boot와 JPA를 사용한 백엔드 개발에 대해 이야기합니다.", PostVisibility.PUBLIC);
            log.info("Post 생성 완료");
            
            log.info("DataInitializer 완료");
        } catch (Exception e) {
            log.error("DataInitializer 실행 중 오류 발생", e);
            throw e;
        }
    }

    private User createUser(String email, String nickname, String password, Set<Role> roles) {
        User user = new User(email, nickname, password, roles);
        return userRepository.save(user);
    }

    private void createPost(User user, String title, String content, PostVisibility visibility) {
        Post post = Post.create(title, content, visibility, user);
        postRepository.save(post);
    }
}
*/
