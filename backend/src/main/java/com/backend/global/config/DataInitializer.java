import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();

        // 계정 시드
        seedAdmin();
        List<User> creators = seedCreators(); // 7명
        seedUsers();                          // 유저 2명

        // 게시글 시드
        seedPostsForCreators(creators);
    }

    /* ================= ROLE ================= */

    private void seedRoles() {
        for (RoleEnum roleEnum : EnumSet.allOf(RoleEnum.class)) {
            roleRepository.findByRole(roleEnum)
                    .orElseGet(() -> roleRepository.save(Role.of(roleEnum)));
        }
    }

    /* ================= USER ================= */

    private void seedAdmin() {
        createUserIfAbsent(
                "admin@test.com",
                "운영자_관리자",
                "1234",
                RoleEnum.ROLE_ADMIN
        );
    }

    private List<User> seedCreators() {
        return List.of(
                createUserIfAbsent("creator-coding@test.com", "코딩멘토_DevGuru", "1234", RoleEnum.ROLE_CREATOR),
                createUserIfAbsent("creator-fitness@test.com", "헬스코치_FitChan", "1234", RoleEnum.ROLE_CREATOR),
                createUserIfAbsent("creator-money@test.com", "재테크_머니민", "1234", RoleEnum.ROLE_CREATOR),
                createUserIfAbsent("creator-art@test.com", "드로잉_아트소연", "1234", RoleEnum.ROLE_CREATOR),
                createUserIfAbsent("creator-cook@test.com", "자취요리_하나쿡", "1234", RoleEnum.ROLE_CREATOR),
                createUserIfAbsent("creator-music@test.com", "보컬쌤_지호", "1234", RoleEnum.ROLE_CREATOR),
                createUserIfAbsent("creator-trip@test.com", "여행브이로그_유나", "1234", RoleEnum.ROLE_CREATOR)
        );
    }

    private void seedUsers() {
        createUserIfAbsent("user1@test.com", "구독왕_팬1호", "1234", RoleEnum.ROLE_USER);
        createUserIfAbsent("user2@test.com", "댓글요정_팬2호", "1234", RoleEnum.ROLE_USER);
    }

    private User createUserIfAbsent(
            String email,
            String nickname,
            String rawPassword,
            RoleEnum roleEnum
    ) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    Role role = roleRepository.findByRole(roleEnum)
                            .orElseThrow(() -> new IllegalStateException("Role not found: " + roleEnum));

                    User user = User.of(
                            email,
                            nickname,
                            passwordEncoder.encode(rawPassword),
                            Set.of(role)
                    );
                    return userRepository.save(user);
                });
    }

    /* ================= POST ================= */

    private void seedPostsForCreators(List<User> creators) {
        for (User creator : creators) {

            List<PostSeed> seeds = PostSeedFactory.forCreator(creator.getNickname());

            for (PostSeed seed : seeds) {
                postRepository.save(
                        Post.create(
                                seed.title(),
                                seed.content(),
                                seed.visibility(),
                                creator
                        )
                );
            }
        }
    }

    private record PostSeed(String title, String content, PostVisibility visibility) {}

    private static class PostSeedFactory {

        static List<PostSeed> forCreator(String nickname) {

            if (nickname.contains("DevGuru")) {
                return List.of(
                        new PostSeed("Spring Boot 레이어드 아키텍처 정리",
                                "Controller-Service-Repository 구조를 왜 쓰는지 실전 관점에서 설명합니다.",
                                PostVisibility.PUBLIC),
                        new PostSeed("JWT 인증 흐름 한 번에 이해하기",
                                "AccessToken / RefreshToken / HttpOnly Cookie 설계 정리.",
                                PostVisibility.PUBLIC),
                        new PostSeed("Docker Compose dev/prod 분리",
                                "env 파일 분리 + healthcheck 구성 팁.",
                                PostVisibility.PUBLIC),
                        new PostSeed("구독자 전용: 배치 스케줄러 테스트 전략",
                                "스케줄러는 통합 테스트로 검증하는 게 안정적입니다.",
                                PostVisibility.SUBSCRIBERS_ONLY)
                );
            }

            if (nickname.contains("FitChan")) {
                return List.of(
                        new PostSeed("하체 루틴 30분",
                                "스쿼트/런지/힙힌지로 구성된 하체 루틴.",
                                PostVisibility.PUBLIC),
                        new PostSeed("단백질 섭취 타이밍",
                                "운동 전/후 단백질 섭취 기준 정리.",
                                PostVisibility.PUBLIC),
                        new PostSeed("구독자 전용: 체형별 루틴 커스터마이징",
                                "체형에 맞는 운동 분배 방법.",
                                PostVisibility.SUBSCRIBERS_ONLY)
                );
            }

            if (nickname.contains("머니민")) {
                return List.of(
                        new PostSeed("월급관리 50/30/20 룰",
                                "생활비/저축/자기계발 예산 관리법.",
                                PostVisibility.PUBLIC),
                        new PostSeed("ETF 처음 시작할 때 주의할 점",
                                "분산투자와 장기 관점이 핵심입니다.",
                                PostVisibility.PUBLIC),
                        new PostSeed("구독자 전용: 월별 예산표 템플릿",
                                "실제 사용하는 예산표 구조 공개.",
                                PostVisibility.SUBSCRIBERS_ONLY)
                );
            }

            if (nickname.contains("아트소연")) {
                return List.of(
                        new PostSeed("아이패드 드로잉 브러시 추천",
                                "선화/채색용 브러시 조합.",
                                PostVisibility.PUBLIC),
                        new PostSeed("캐릭터 얼굴 비율 쉽게 잡는 법",
                                "기본 가이드라인 설명.",
                                PostVisibility.PUBLIC),
                        new PostSeed("구독자 전용: 포스터 레이아웃 레퍼런스",
                                "실제 작업 순서와 예시.",
                                PostVisibility.SUBSCRIBERS_ONLY)
                );
            }

            if (nickname.contains("하나쿡")) {
                return List.of(
                        new PostSeed("5분 완성 간장계란밥",
                                "자취생 필수 초간단 레시피.",
                                PostVisibility.PUBLIC),
                        new PostSeed("자취생 냉장고 관리법",
                                "유통기한/소분/냉동 팁.",
                                PostVisibility.PUBLIC),
                        new PostSeed("구독자 전용: 일주일 장보기 리스트",
                                "현실적인 장보기 목록 공유.",
                                PostVisibility.SUBSCRIBERS_ONLY)
                );
            }

            if (nickname.contains("지호")) {
                return List.of(
                        new PostSeed("고음이 안 올라갈 때 체크 포인트",
                                "호흡과 공명이 핵심입니다.",
                                PostVisibility.PUBLIC),
                        new PostSeed("발성 연습 10분 루틴",
                                "매일 할 수 있는 연습 루틴.",
                                PostVisibility.PUBLIC),
                        new PostSeed("구독자 전용: 노래별 키 조절 팁",
                                "자기 음역대 찾는 법.",
                                PostVisibility.SUBSCRIBERS_ONLY)
                );
            }

            // 여행브이로그_유나
            return List.of(
                    new PostSeed("주말 국내 여행 코스 추천",
                            "당일치기/1박2일 코스 제안.",
                            PostVisibility.PUBLIC),
                    new PostSeed("짐 싸기 체크리스트",
                            "여행 전 필수 준비물.",
                            PostVisibility.PUBLIC),
                    new PostSeed("구독자 전용: 브이로그 촬영 세팅",
                            "카메라/마이크/촬영 루틴.",
                            PostVisibility.SUBSCRIBERS_ONLY)
            );
        }
    }
}