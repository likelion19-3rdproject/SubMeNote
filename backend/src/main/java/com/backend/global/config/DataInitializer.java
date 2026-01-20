package com.backend.global.config;

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
                        new PostSeed(
                                "Spring Boot 레이어드 아키텍처 정리",
                                """
                                Spring Boot에서 Controller–Service–Repository 구조를 사용하는 이유는
                                단순히 코드 정리를 위해서가 아닙니다.
                                이 구조의 핵심 목적은 변경에 강한 시스템을 만들기 위한 책임 분리입니다.
                    
                                Controller는 요청과 응답에만 집중해야 하며,
                                비즈니스 판단이나 데이터 처리 로직이 섞이기 시작하면
                                테스트와 유지보수가 급격히 어려워집니다.
                    
                                Service는 비즈니스 규칙의 중심입니다.
                                트랜잭션 경계, 정책 판단, 여러 도메인의 협력이 이 계층에서 이루어져야 하며,
                                이 책임이 무너지면 레이어 구조는 사실상 의미를 잃습니다.
                    
                                Repository는 데이터를 가져오는 역할에만 충실해야 합니다.
                                조회 조건이 복잡해진다고 해서
                                비즈니스 의미를 Repository에 밀어 넣는 순간 구조는 서서히 망가집니다.
                    
                                실제 프로젝트에서 레이어 경계가 무너졌을 때
                                어떤 문제가 발생했는지, 그리고 이를 어떻게 바로잡았는지를
                                실전 경험을 기준으로 정리합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "JWT 인증 흐름 한 번에 이해하기",
                                """
                                JWT 인증을 처음 접할 때 가장 많이 오해하는 부분은
                                AccessToken과 RefreshToken의 관계입니다.
                                단순히 “유효기간이 다른 토큰”으로 이해하면
                                보안 설계는 거의 항상 실패합니다.
                    
                                AccessToken은 빠른 인증을 위한 도구이며,
                                RefreshToken은 인증 상태를 유지하기 위한 안전장치입니다.
                                두 토큰의 책임을 분리하지 않으면
                                토큰 탈취 시 피해 범위가 걷잡을 수 없이 커집니다.
                    
                                HttpOnly Cookie를 사용하는 이유 역시 단순한 편의가 아닙니다.
                                XSS 공격으로부터 토큰을 보호하기 위한 최소한의 방어선이며,
                                프론트엔드에서 토큰을 직접 다루지 않게 만드는 중요한 장치입니다.
                    
                                또한 토큰 재발급 시점에서
                                서버가 반드시 검증해야 할 조건들이 존재합니다.
                                이를 놓치면 “로그인 풀림”, “인증 꼬임” 같은 문제가 반복됩니다.
                    
                                이 글에서는 실제 인증 흐름을 기준으로
                                JWT 설계를 어떻게 가져가야 안정적인지 정리합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "Docker Compose dev/prod 분리 전략",
                                """
                                Docker Compose를 dev와 prod로 나누는 이유는
                                환경 설정을 깔끔하게 하기 위해서만은 아닙니다.
                                가장 큰 목적은 운영 사고를 예방하는 데 있습니다.
                    
                                개발 환경에서는 편의성이 중요하지만,
                                운영 환경에서는 예측 가능성과 안정성이 최우선입니다.
                                이 차이를 무시하고 하나의 compose 파일로 관리하면
                                배포 시점에 반드시 문제가 발생합니다.
                    
                                env 파일 분리는 단순한 변수 관리가 아니라
                                비밀 정보 노출을 막기 위한 기본 장치입니다.
                                특히 DB, 외부 API 키, 인증 정보는
                                환경별로 명확히 분리되어야 합니다.
                    
                                healthcheck는 선택이 아니라 필수입니다.
                                컨테이너가 “떠 있다”는 것과
                                “정상적으로 서비스 가능한 상태”는 전혀 다르기 때문입니다.
                    
                                실무에서 자주 터지는 설정 실수 사례와 함께
                                안정적인 compose 구성 전략을 정리합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "구독자 전용: 배치 스케줄러 테스트 전략",
                                """
                                배치 스케줄러는 개발 단계에서는 잘 보이지 않지만,
                                운영 환경에서 가장 치명적인 장애를 일으키는 영역 중 하나입니다.
                    
                                실행 시점이 시간에 의존하고,
                                중복 실행이나 데이터 정합성 문제가 쉽게 발생하기 때문에
                                단위 테스트만으로는 신뢰하기 어렵습니다.
                    
                                많은 팀이 “문제가 생기면 그때 고치자”는 접근을 택하지만,
                                배치 장애는 이미 데이터가 망가진 뒤에야 드러납니다.
                    
                                그래서 스케줄러는 통합 테스트 관점에서 검증해야 합니다.
                                실제 트랜잭션 흐름, DB 상태 변화,
                                그리고 재실행 시나리오까지 함께 확인해야 합니다.
                    
                                이 글에서는 테스트 환경에서 스케줄러를 안전하게 검증하는 방법과
                                운영 전에 반드시 점검해야 할 기준들을 정리합니다.
                                """,
                                PostVisibility.SUBSCRIBERS_ONLY
                        )
                );
            }


            if (nickname.contains("FitChan")) {
                return List.of(
                        new PostSeed(
                                "하체 루틴 30분",
                                """
                                이 하체 루틴은 헬스 초중급자를 기준으로,
                                짧은 시간 안에 하체 전체를 고르게 자극하는 것을 목표로 구성했습니다.
                    
                                스쿼트, 런지, 힙힌지 동작을 중심으로
                                대퇴사두근, 햄스트링, 둔근을 모두 사용하는 구조입니다.
                                특정 부위만 과하게 쓰지 않도록
                                동작 순서와 볼륨을 조절했습니다.
                    
                                각 동작은 고중량보다는
                                정확한 자세와 컨트롤을 우선으로 합니다.
                                무게 욕심을 내기보다는
                                마지막 세트까지 자세가 유지되는 중량을 선택하세요.
                    
                                워밍업 포함 30분이면 충분히 끝낼 수 있으며,
                                하체 운동 후 피로도가 과도하게 쌓이지 않도록 설계했습니다.
                                바쁜 날에도 꾸준히 가져갈 수 있는 현실적인 루틴입니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "단백질 섭취 타이밍",
                                """
                                단백질 섭취는 양만큼이나 타이밍이 중요합니다.
                                하지만 생각보다 복잡하게 접근할 필요는 없습니다.
                    
                                운동 전에는
                                소화가 부담되지 않는 가벼운 단백질 섭취가 적당합니다.
                                공복 상태로 운동하는 경우,
                                근손실 위험이 커질 수 있기 때문입니다.
                    
                                운동 후에는
                                근육 회복과 합성을 위해 단백질 섭취가 필수적입니다.
                                이때 ‘30분 골든타임’에 집착하기보다는,
                                운동 후 1~2시간 내 안정적인 섭취를 목표로 하세요.
                    
                                하루 총 단백질 섭취량을 채우는 것이
                                특정 타이밍보다 훨씬 중요합니다.
                                본인의 생활 패턴에 맞게
                                꾸준히 유지할 수 있는 방식을 선택하는 것이 핵심입니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "구독자 전용: 체형별 루틴 커스터마이징",
                                """
                                같은 루틴을 해도 결과가 다른 이유는
                                체형과 신체 비율이 모두 다르기 때문입니다.
                    
                                하체가 발달한 체형, 상체에 비해 둔근이 약한 체형,
                                관절 가동성이 제한된 체형은
                                운동 분배 방식부터 달라져야 합니다.
                    
                                무조건적인 스쿼트 반복은
                                어떤 사람에게는 도움이 되지만,
                                어떤 사람에게는 부상 위험만 키울 수 있습니다.
                    
                                이 글에서는
                                체형별로 어떤 동작 비중을 줄이고,
                                어떤 동작을 보완해야 하는지 기준을 제시합니다.
                    
                                “열심히 했는데 몸이 안 바뀌는 이유”를
                                루틴 관점에서 점검하고 싶은 분들을 위한 콘텐츠입니다.
                                """,
                                PostVisibility.SUBSCRIBERS_ONLY
                        )
                );
            }


            if (nickname.contains("머니민")) {
                return List.of(
                        new PostSeed(
                                "월급관리 50/30/20 룰",
                                """
                                50/30/20 룰은 재테크 전략이 아니라
                                월급을 망치지 않기 위한 최소한의 기준선입니다.
                    
                                수입의 50%는 생활비,
                                30%는 여유 지출,
                                20%는 저축과 미래를 위한 비용으로 나누는 방식입니다.
                                비율 자체보다 중요한 것은
                                돈의 용도를 명확히 구분하는 데 있습니다.
                    
                                많은 사람들이 저축에 실패하는 이유는
                                돈이 남으면 하겠다는 방식으로 접근하기 때문입니다.
                                실제로는 생활비와 여유 지출이 먼저 늘어나고,
                                저축은 항상 뒤로 밀립니다.
                    
                                이 룰은 “남은 돈을 모으는 방식”이 아니라
                                “먼저 떼어두고 쓰는 방식”으로
                                소비 구조를 바꾸는 데 목적이 있습니다.
                    
                                개인 상황에 따라 비율은 조정할 수 있지만,
                                예산을 나눠서 관리하는 원칙만큼은
                                반드시 지키는 것이 핵심입니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "ETF 처음 시작할 때 주의할 점",
                                """
                                ETF는 접근하기 쉬운 투자 수단이지만,
                                그렇다고 해서 아무 생각 없이 시작해도 되는 것은 아닙니다.
                    
                                가장 중요한 개념은 분산투자입니다.
                                하나의 종목이나 특정 섹터에 집중된 ETF는
                                개별 주식과 큰 차이가 없을 수 있습니다.
                    
                                또한 ETF 투자는
                                단기 수익을 노리기보다는
                                시간과 함께 가져가는 방식이 기본입니다.
                                잦은 매매는 수익률을 깎는 가장 빠른 방법입니다.
                    
                                많은 초보 투자자들이
                                수익률이 좋았던 ETF를 뒤늦게 따라 사는 실수를 합니다.
                                하지만 과거 수익률은
                                미래를 보장하지 않는다는 점을 항상 염두에 두어야 합니다.
                    
                                ETF는 ‘안전한 투자’가 아니라
                                ‘관리하기 쉬운 투자’에 가깝다는 점을
                                분명히 인식하고 시작하는 것이 중요합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "구독자 전용: 월별 예산표 템플릿",
                                """
                                예산 관리는 의지만으로는 절대 유지되지 않습니다.
                                결국 남는 것은 기록이고,
                                기록을 가능하게 만드는 것은 구조입니다.
                    
                                이 글에서는
                                제가 실제로 사용하고 있는 월별 예산표 구조를 공개합니다.
                                수입, 고정 지출, 변동 지출, 저축 항목을
                                한눈에 확인할 수 있도록 구성되어 있습니다.
                    
                                특히 월 중간에 예산이 무너졌을 때도
                                흐름을 다시 잡을 수 있도록
                                보완 지출 항목과 조정 영역을 따로 두었습니다.
                    
                                완벽하게 지키는 예산표가 아니라,
                                현실적으로 계속 쓸 수 있는 예산표를 목표로 했습니다.
                    
                                가계부를 여러 번 포기해본 분들이라면,
                                왜 이 구조가 부담을 줄여주는지
                                바로 체감할 수 있을 것입니다.
                                """,
                                PostVisibility.SUBSCRIBERS_ONLY
                        )
                );
            }


            if (nickname.contains("아트소연")) {
                return List.of(
                        new PostSeed(
                                "아이패드 드로잉 브러시 추천",
                                """
                                아이패드 드로잉에서 브러시는 많다고 좋은 게 아닙니다.
                                오히려 브러시가 많을수록 선이 흔들리고,
                                작업 속도가 느려지는 경우가 많습니다.
                    
                                이 글에서는 실제 작업에서 자주 사용하는
                                선화용 브러시와 채색용 브러시 조합을 기준으로 정리했습니다.
                                선이 너무 날카롭거나, 반대로 너무 뭉개지지 않도록
                                컨트롤하기 쉬운 브러시 위주로 구성했습니다.
                    
                                특히 선화 브러시는
                                손떨림 보정 수치와 필압 반응이 중요합니다.
                                자신에게 맞지 않는 브러시는
                                실력보다 결과물을 먼저 망가뜨립니다.
                    
                                브러시를 바꾸는 것보다 중요한 것은
                                최소한의 브러시로 반복해서 익숙해지는 과정입니다.
                                입문자와 중급자 모두 부담 없이 쓸 수 있는 조합을 기준으로
                                설명합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "캐릭터 얼굴 비율 쉽게 잡는 법",
                                """
                                캐릭터 얼굴이 어색해 보이는 이유는
                                대부분 비율이 무너졌기 때문입니다.
                                선을 못 그려서라기보다는
                                기준 없이 그리기 시작했기 때문인 경우가 많습니다.
                    
                                이 글에서는 복잡한 해부학 대신,
                                바로 적용할 수 있는 기본 가이드라인을 중심으로 설명합니다.
                                얼굴의 중심선, 눈 위치, 턱 길이만 안정되어도
                                전체 인상이 크게 달라집니다.
                    
                                특히 정면과 3/4 각도에서
                                가장 많이 틀어지는 지점을 짚어봅니다.
                                눈 간격, 코 위치, 입 위치를
                                동시에 잡으려 하지 않는 것이 핵심입니다.
                    
                                빠르게 스케치할 때도
                                최소한의 기준선을 먼저 긋는 습관이
                                작업 안정도를 크게 높여줍니다.
                                연습할 때 바로 써먹을 수 있는 방식으로 정리합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "구독자 전용: 포스터 레이아웃 레퍼런스",
                                """
                                포스터 작업에서 가장 어려운 부분은
                                그림을 그리는 것보다 레이아웃을 정하는 단계입니다.
                                이 단계에서 방향을 잘못 잡으면
                                끝까지 어색함이 따라옵니다.
                    
                                이 글에서는 실제 작업을 기준으로
                                레퍼런스를 어떻게 고르고,
                                어떤 순서로 배치 구조를 잡는지 설명합니다.
                                처음부터 디테일을 넣지 않고,
                                덩어리와 흐름부터 잡는 과정을 보여줍니다.
                    
                                메인 이미지, 보조 요소, 텍스트의 관계를
                                한 번에 해결하려 하지 않고
                                단계별로 나누는 것이 핵심입니다.
                    
                                완성된 예시뿐 아니라
                                중간 과정에서 어떤 고민을 했는지도 함께 담았습니다.
                                포스터 작업이 막막했던 분들에게
                                실제 작업 흐름을 참고 자료로 제공하는 콘텐츠입니다.
                                """,
                                PostVisibility.SUBSCRIBERS_ONLY
                        )
                );
            }

            if (nickname.contains("하나쿡")) {
                return List.of(
                        new PostSeed(
                                "5분 완성 간장계란밥",
                                """
                                간장계란밥은 요리라기보다는
                                자취 생활에서 살아남기 위한 기본기 같은 메뉴입니다.
                    
                                밥, 계란, 간장만 있으면 되지만
                                순서와 불 조절에 따라 결과가 꽤 달라집니다.
                                계란을 너무 익히면 퍽퍽해지고,
                                반대로 덜 익히면 비리게 느껴질 수 있습니다.
                    
                                이 레시피는 설거지를 최소화하고
                                조리 시간을 5분 안으로 끝내는 데 초점을 맞췄습니다.
                                팬 하나로 해결할 수 있도록 구성했습니다.
                    
                                간장 양은 많이 넣는 것보다
                                마지막에 조금씩 조절하는 게 핵심입니다.
                                김가루나 참기름은 선택 사항이고,
                                없어도 충분히 먹을 수 있는 기준으로 설명합니다.
                    
                                “요리하기 귀찮은 날에도
                                최소한 굶지는 않게 해주는 메뉴”로
                                자주 써먹을 수 있는 방식입니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "자취생 냉장고 관리법",
                                """
                                자취하면서 음식이 자주 상하는 이유는
                                요리를 못해서가 아니라
                                냉장고 관리가 안 되기 때문인 경우가 많습니다.
                    
                                냉장고 안에 뭐가 있는지 모르는 상태에서는
                                결국 같은 재료를 또 사고,
                                이미 있는 음식은 방치되기 쉽습니다.
                    
                                이 글에서는
                                유통기한을 기억하지 않아도 되는 배치 방법과
                                소분해서 보관하는 최소한의 기준을 정리합니다.
                                특히 냉동실을 ‘비상식량 창고’처럼 쓰는 방식이 핵심입니다.
                    
                                모든 재료를 깔끔하게 정리하려고 하면
                                오히려 관리가 오래가지 않습니다.
                                귀찮아도 유지할 수 있는 수준이 중요합니다.
                    
                                냉장고를 한 번 정리해두면
                                장보기와 식비 관리가 동시에 편해지는 구조를
                                자취 기준으로 설명합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "구독자 전용: 일주일 장보기 리스트",
                                """
                                장보기에서 가장 큰 실패는
                                계획 없이 마트에 들어가는 순간부터 시작됩니다.
                    
                                이 장보기 리스트는
                                일주일 동안 최소한의 요리를 기준으로
                                실제 자취 생활에 맞게 구성되어 있습니다.
                                요리 잘하는 사람 기준이 아니라,
                                귀찮은 날을 전제로 한 목록입니다.
                    
                                한 번 사두면 여러 끼에 나눠 쓸 수 있는 재료와
                                바로 먹을 수 있는 안전한 메뉴를 함께 섞었습니다.
                                냉동 가능한 재료와
                                빨리 소비해야 할 재료를 구분해두었습니다.
                    
                                식비를 줄이기 위한 리스트라기보다는,
                                불필요한 추가 구매를 막기 위한 구조에 가깝습니다.
                    
                                “뭘 사야 할지 몰라서
                                항상 장보기가 부담됐던 자취생”을 위한
                                현실적인 기준표를 공유합니다.
                                """,
                                PostVisibility.SUBSCRIBERS_ONLY
                        )
                );
            }


            if (nickname.contains("지호")) {
                return List.of(
                        new PostSeed(
                                "고음이 안 올라갈 때 체크 포인트",
                                """
                                고음이 안 올라갈 때
                                대부분은 성대가 약해서라고 생각하지만,
                                실제로는 호흡과 공명 문제인 경우가 많습니다.
                    
                                숨을 충분히 쓰지 못한 상태에서
                                소리를 억지로 끌어올리면
                                목에 힘이 먼저 들어가게 됩니다.
                                이 상태에서는 고음이 올라가도
                                금방 지치고 음정이 불안해집니다.
                    
                                고음을 내기 전에
                                소리가 앞으로 나가는지,
                                턱과 목 주변에 불필요한 긴장이 없는지를
                                먼저 점검해보는 것이 중요합니다.
                    
                                특히 입을 크게 여는 것보다
                                공명이 머물 공간을 만들어주는 느낌이
                                고음에서는 훨씬 도움이 됩니다.
                    
                                고음을 ‘밀어 올리는 음’이 아니라
                                ‘지나가게 하는 음’으로 인식하는 순간,
                                체감이 달라지기 시작합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "발성 연습 10분 루틴",
                                """
                                발성 연습은 오래 하는 것보다
                                매일 짧게 꾸준히 하는 것이 훨씬 효과적입니다.
                                특히 노래를 취미로 하는 사람에게는
                                부담 없는 루틴이 중요합니다.
                    
                                이 루틴은
                                워밍업부터 발성, 마무리까지
                                10분 안에 끝낼 수 있도록 구성했습니다.
                                목을 혹사시키지 않으면서
                                소리 감각을 깨우는 데 초점을 둡니다.
                    
                                처음부터 큰 소리를 내기보다는
                                가볍게 연결되는 소리로 시작하는 것이 핵심입니다.
                                소리가 걸리는 지점을
                                억지로 뚫으려 하지 않아도 됩니다.
                    
                                중요한 것은
                                연습을 끝냈을 때 목이 편안한 상태인지입니다.
                                연습 후에도 목이 뻐근하다면
                                이미 과하게 사용한 신호일 수 있습니다.
                    
                                매일 노래하기 전,
                                혹은 노래를 못 하는 날에도
                                부담 없이 가져갈 수 있는 루틴으로 설명합니다.
                                """,
                                PostVisibility.PUBLIC
                        ),

                        new PostSeed(
                                "구독자 전용: 노래별 키 조절 팁",
                                """
                                많은 사람들이
                                원곡 키로 노래를 부르지 못하면
                                실력이 부족하다고 생각합니다.
                                하지만 키 선택은 실력 문제가 아니라
                                자기 음역대를 이해하는 문제에 가깝습니다.
                    
                                무리해서 고음을 내는 키는
                                단기적으로는 가능할 수 있어도
                                목을 빠르게 지치게 만듭니다.
                                특히 라이브나 연속으로 노래할 때
                                차이가 크게 드러납니다.
                    
                                이 글에서는
                                노래별로 어디를 기준으로 키를 내리거나 올릴지,
                                단순히 반 키, 한 키가 아니라
                                ‘가장 안정적인 구간’을 찾는 방법을 설명합니다.
                    
                                자신의 최고음이 아니라
                                가장 편하게 소리를 유지할 수 있는 음역대를
                                기준으로 키를 정하는 것이 핵심입니다.
                    
                                노래를 오래 하고 싶다면,
                                잘 들리는 키보다
                                계속 부를 수 있는 키를 선택해야 합니다.
                                """,
                                PostVisibility.SUBSCRIBERS_ONLY
                        )
                );
            }


            // 여행브이로그_유나
            return List.of(
                    new PostSeed(
                            "주말 국내 여행 코스 추천",
                            """
                            주말 여행에서 가장 중요한 건
                            얼마나 멀리 가느냐가 아니라
                            이동 대비 만족도가 얼마나 나오느냐입니다.
                    
                            이 글에서는
                            당일치기와 1박 2일 기준으로
                            이동 시간, 동선, 체력 소모를 함께 고려한
                            현실적인 국내 여행 코스를 정리했습니다.
                    
                            무조건 유명한 곳보다는
                            대중교통이나 자차로 접근하기 쉬운 지역,
                            일정이 빡빡하지 않아도
                            여행 느낌이 나는 코스를 중심으로 구성했습니다.
                    
                            특히 주말에 자주 발생하는
                            교통 체증, 식당 대기, 숙소 체크인 시간 같은 변수도
                            일정에 반영해 설명합니다.
                    
                            “쉬려고 갔다가 더 피곤해지는 여행”을 피하고 싶은 분들을 위한,
                            반복해서 써먹을 수 있는 주말 여행 기준표입니다.
                            """,
                            PostVisibility.PUBLIC
                    ),

                    new PostSeed(
                            "짐 싸기 체크리스트",
                            """
                            여행 준비에서 가장 많이 하는 실수는
                            필요 없는 물건은 잔뜩 챙기고,
                            꼭 필요한 물건을 빠뜨리는 것입니다.
                    
                            이 체크리스트는
                            여행 기간과 숙소 유형에 따라
                            반드시 필요한 물건만 남기도록 구성했습니다.
                            ‘혹시 몰라서’ 넣는 물건은
                            최대한 줄이는 방향입니다.
                    
                            의류, 세면도구, 전자기기처럼
                            항목별로 나누되,
                            현지에서 대체 가능한 것과
                            반드시 챙겨야 하는 것을 구분해 설명합니다.
                    
                            짐을 줄이면 이동이 편해지고,
                            여행 중 선택지도 훨씬 단순해집니다.
                            여행을 가볍게 시작하고 싶은 사람을 위한
                            최소 기준 체크리스트입니다.
                            """,
                            PostVisibility.PUBLIC
                    ),

                    new PostSeed(
                            "구독자 전용: 브이로그 촬영 세팅",
                            """
                            여행 브이로그는
                            장비보다 루틴이 더 중요합니다.
                            장비를 많이 챙길수록
                            오히려 촬영을 포기하게 되는 경우가 많습니다.
                    
                            이 글에서는
                            여행 중 실제로 사용하기 부담 없는
                            카메라, 마이크 조합과
                            이동 상황에 맞춘 촬영 세팅을 정리했습니다.
                    
                            숙소, 이동 중, 야외 촬영 등
                            상황별로 어떤 세팅이 현실적인지,
                            촬영을 놓치지 않기 위한 최소 루틴을 설명합니다.
                    
                            “잘 찍어야지”보다는
                            “끝까지 찍을 수 있는 세팅”을 기준으로 구성했습니다.
                            여행도 즐기면서
                            기록도 남기고 싶은 분들을 위한 가이드입니다.
                            """,
                            PostVisibility.SUBSCRIBERS_ONLY
                    )
            );

        }
    }
}