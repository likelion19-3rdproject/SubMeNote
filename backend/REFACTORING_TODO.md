# 백엔드 리팩토링 TODO

## 리팩토링 우선순위 목록

1. ResponseCookie 생성 중복 제거
2. User 조회 패턴 중복 제거
3. 구독 검증 로직 중복 제거
4. PostServiceImpl과 CommentServiceImpl 공통 메서드 분리
5. 회원가입/로그인/로그아웃/회원탈퇴 공통 메서드
6. Config 파일 위치 통일
7. CustomUserDetails 클래스 중복 해결
8. @Transactional 누락 메서드 체크
9. User 조회 패턴 중복 제거 (Repository 메서드 추가)
10. DTO 변환 방식 통일
11. Pageable 파라미터 사용 방식 통일
12. CustomUserDetails.getUserId() 사용 방식 통일
13. 크리에이터 권한 체크 중복 제거
14. 로그인 체크 로직 중복 제거
15. 엔티티 생성 방식 통일
16. DTO 파일명 및 미사용 DTO 정리
17. SecurityConfig 리팩터링
18. Admin 관련 기능 통합
19. 주석 형식 통일
20. 매직 넘버 상수화
21. CustomUserDetails에 nickname 필드 추가 검토

---

## 1. ResponseCookie 생성 중복 제거

### 문제
쿠키 생성 로직이 여러 컨트롤러에서 중복됨
- `AuthController.java`: 6곳 (29-35, 37-43, 62-68, 71-77, 95-101, 103-109, 136-143, 145-152)
- `UserController.java`: 2곳 (63-69, 72-78)

### 중복 코드 예시
```java
ResponseCookie accessCookie = ResponseCookie.from("accessToken", result.accessToken())
    .httpOnly(true)
    .path("/")
    .maxAge(60 * 30)
    .secure(false)
    .sameSite("Lax")
    .build();
```

### 해결 방안
`global/util/CookieUtil.java` 유틸리티 클래스 생성
- `createAccessTokenCookie(String token)`: AccessToken 쿠키 생성
- `createRefreshTokenCookie(String token)`: RefreshToken 쿠키 생성
- `deleteAccessTokenCookie()`: AccessToken 쿠키 삭제
- `deleteRefreshTokenCookie()`: RefreshToken 쿠키 삭제
- 쿠키 설정값 (maxAge, path 등) 상수화

---

## 2. User 조회 패턴 중복 제거

### 문제
`userRepository.findById().orElseThrow()` 패턴이 반복됨
- `AuthServiceImpl.java`: 1곳 (47번째 줄)
- `UserServiceImpl.java`: 6곳 (60, 81, 133, 158, 176, 205)
- `PostServiceImpl.java`: 3곳 (46, 67, 85)
- `CommentServiceImpl.java`: 1곳 (59번째 줄)
- `LikeServiceImpl.java`: 1곳 (46번째 줄)
- 기타 여러 파일에서 반복

### 해결 방안
**방안 1**: Repository에 메서드 추가
```java
public interface UserRepository extends JpaRepository<User, Long> {
    default User findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
```

**방안 2**: UserService에 헬퍼 메서드 추가
```java
public User getUserOrThrow(Long userId) { ... }
```

---

## 3. 구독 검증 로직 중복 제거

### 문제
- `PostServiceImpl.java`에 `validateSubscription` 메서드 (228-239번째 줄)
- `SubscribeServiceImpl.java`에 `validateSubscription` 메서드 (150-168번째 줄)
- 거의 동일한 로직이 두 곳에 존재

### 해결 방안
`SubscribeService`의 메서드를 사용하도록 통일
- `PostServiceImpl`에서 자체 `validateSubscription` 메서드 제거
- `subscribeService.validateSubscription()` 사용

---

## 4. PostServiceImpl과 CommentServiceImpl 공통 메서드 분리

### 문제
`validatePostAccess`와 `validateReadPermission` 메서드가 거의 동일한 로직
- `PostServiceImpl.java`: `validateReadPermission` (203-224번째 줄)
- `CommentServiceImpl.java`: `validatePostAccess` (142-162번째 줄)
- 거의 동일한 검증 로직 (로그인 체크 → 작성자 확인 → 구독 확인 → 유료 글 체크)

### 해결 방안
- `PostService` 또는 `PostAccessService`에 공통 메서드로 추출
- `CommentService`에서 `PostService`의 메서드 재사용
- 또는 `AuthorizationService`로 분리하여 공통 사용

---

## 5. 회원가입/로그인/로그아웃/회원탈퇴 공통 메서드

### 문제
인증 관련 컨트롤러에서 공통 로직 중복
- 쿠키 삭제 로직: `AuthController.logout()`, `UserController.signout()`에서 중복
- 토큰 발급 및 쿠키 설정: `AuthController.login()`, `AuthController.signup()`, `AuthController.refresh()`에서 중복
- 사용자 검증: 여러 곳에서 반복

### 해결 방안
- 쿠키 관련은 `CookieUtil`로 분리 (1번 항목 참고)
- 토큰 발급 및 설정 로직을 `AuthService`에 통합 메서드로 분리
- 공통 검증 로직을 유틸리티 메서드로 분리

---

## 6. Config 파일 위치 통일

### 문제
Config 파일들이 여러 위치에 분산되어 있음
- `payment/RestClientConfig.java` → `global/config/`로 이동 필요
- `global/SecurityConfig.java` → `global/config/`로 이동 검토
- `global/JpaAuditingConfig.java` → `global/config/`로 이동 검토
- `global/MailConfig.java` → `global/config/`로 이동 검토
- `global/RedisConfig.java` → `global/config/`로 이동 검토

### 권장 구조
```
global/
  config/
    CorsConfig.java
    JpaAuditingConfig.java
    MailConfig.java
    RedisConfig.java
    RestClientConfig.java
    SecurityConfig.java
  exception/
  jwt/
  properties/
  util/
```

---

## 7. CustomUserDetails 클래스 중복 해결

### 문제
`CustomUserDetails` 클래스가 두 곳에 존재
- `user/entity/CustomUserDetails.java` (User 엔티티를 직접 참조)
- `global/util/CustomUserDetails.java` (필드만 보유, JWT용)

### 영향
- 두 클래스의 목적이 다름
- `global/util/CustomUserDetails`만 사용 중 (JWT 인증)
- `user/entity/CustomUserDetails`는 사용되지 않는 것으로 보임

### 해결 방안
- 사용되지 않는 `user/entity/CustomUserDetails.java` 삭제
- 또는 통합 검토 (단, 목적이 다르므로 분리 유지 권장)

---

## 8. @Transactional 누락 메서드 체크

### 문제
- 클래스 레벨에 선언 (예: `PostServiceImpl`, `CommentServiceImpl`, `LikeServiceImpl`)
- 메서드 레벨에 선언 (예: `UserServiceImpl`, `AuthServiceImpl`)
- 클래스 레벨 선언 시 일부 메서드에 `@Transactional(readOnly = true)` 누락 가능성

### 해결 방안
- 메서드 레벨 선언으로 통일 (readOnly 속성 명시 가능)
- 모든 Service 메서드에 `@Transactional` 또는 `@Transactional(readOnly = true)` 명시
- 클래스 레벨 선언이 있는 경우, 모든 메서드에 명시적으로 적용 여부 확인

---

## 9. DTO 변환 방식 통일

### 문제
- 일부는 정적 팩토리 메서드 `.from()` 사용 (예: `UserResponseDto.from(user)`)
- 일부는 생성자 직접 사용 (예: `new CreatorResponseDto(id, nickname)`)

### 영향 받는 파일
- `UserServiceImpl.java` (51번째 줄, 249번째 줄)
- `PostServiceImpl.java` (241-258번째 줄)
- 기타 DTO 클래스들

### 해결 방안
모든 DTO에서 정적 팩토리 메서드 `.from()` 패턴으로 통일

---

## 10. Pageable 파라미터 사용 방식 통일

### 문제
페이지네이션 파라미터가 일관되지 않음
- 일부는 `Pageable` 직접 사용 (예: `searchCreators(String keyword, Pageable pageable)`)
- 일부는 `(int page, int size)` 사용 후 내부에서 Pageable 생성 (예: `listAllCreators(int page, int size)`)

### 영향 받는 파일
- `UserService.java`: `listAllCreators(int page, int size)` vs `searchCreators(String keyword, Pageable pageable)`
- `UserServiceImpl.java`: 동일
- Controller에서 `@PageableDefault` 사용 시와 수동 생성 시 혼재

### 해결 방안
- Service 인터페이스와 구현체에서 `Pageable` 직접 사용으로 통일
- Controller에서 `@PageableDefault` 활용
- 필요시 내부에서 기본값 설정

---

## 11. CustomUserDetails.getUserId() 사용 방식 통일

### 문제
`CustomUserDetails`에서 userId 추출 방식이 불일치
- 일부는 `userDetails.getUserId()` 직접 사용
- 일부는 삼항 연산자 사용: `(userDetails != null) ? userDetails.getUserId() : null`

### 영향 받는 파일
- `PostController.java`: 66, 78, 91, 103번째 줄 (삼항 연산자 사용)
- `UserController.java`: 직접 사용

### 해결 방안
- 일관된 null 체크 패턴 수립
- Controller 레벨에서 null 처리 후 Service로 전달
- 또는 별도 헬퍼 메서드 생성 (`AuthUtil.extractUserId(CustomUserDetails)`)

---

## 12. 크리에이터 권한 체크 중복 제거

### 문제
`!user.hasRole(RoleEnum.ROLE_CREATOR)` 체크가 반복됨
- `UserServiceImpl.java`: 3곳 (136, 161, 179)
- `PostServiceImpl.java`: 1곳 (49번째 줄)

### 해결 방안
`UserService`에 권한 검증 메서드 추가
```java
public void validateCreatorRole(Long userId) { ... }
```

---

## 13. 로그인 체크 로직 중복 제거

### 문제
`currentUserId == null` 체크가 여러 곳에서 반복됨
- `PostServiceImpl.java`: 3곳 (103, 128, 153, 205)
- `PostController.java`: 3곳 (66, 78, 91, 103)

### 해결 방안
- Controller 레벨에서 처리하거나, 별도 유틸리티 메서드 생성
- `AuthUtil.validateLogin(Long userId)` 같은 헬퍼 메서드 활용

---

## 14. 엔티티 생성 방식 통일

### 문제
엔티티 생성 방식이 혼재됨
- 일부는 `static` 팩토리 메서드 사용 (예: `Post.create()`, `Comment.create()`)
- 일부는 `new` 생성자 직접 사용 (예: `new User()`, `new Account()`, `new Role()`)

### 영향 받는 파일
- `Post.java`: `Post.create()` 메서드 사용 ✅
- `Comment.java`: `Comment.create()` 메서드 사용 ✅
- `User.java`: `new User()` 직접 사용
- `Account.java`: `new Account()` 직접 사용
- `Role.java`: `new Role()` 직접 사용
- `BackendApplication.java`: `new User()`, `new Role()` 사용

### 해결 방안
모든 엔티티에서 정적 팩토리 메서드 패턴으로 통일 (생성자 캡슐화)

---

## 15. DTO 파일명 및 미사용 DTO 정리

### 문제
- `paymentDto` 등 파일명이 규칙에 맞지 않음 (예: `PaymentDto.java` 형식 권장)
- 일부 DTO가 사용되지 않을 가능성

### 해결 방안
- 파일명 규칙 통일: `*RequestDto.java`, `*ResponseDto.java`
- 미사용 DTO 확인 및 정리

---

## 16. SecurityConfig 리팩터링

### 문제
`SecurityConfig.java`가 복잡하고 가독성이 떨어질 수 있음
- 현재 위치: `global/SecurityConfig.java` (6번 항목에서 `global/config/`로 이동 예정)

### 해결 방안
- Security 관련 설정을 여러 메서드로 분리
- Security 설정 상수 클래스 분리
- 각 보안 규칙을 별도 메서드로 추출하여 가독성 향상
- 필터 체인 설정을 명확하게 구조화

---

## 17. Admin 관련 기능 통합

### 문제
Admin 관련 기능이 여러 곳에 분산되어 있음
- `user/controller/AdminController.java`: 크리에이터 신청 승인, 사용자 관리
- `user/service/AdminService.java`, `AdminServiceImpl.java`: Admin 서비스
- `notification/controller/AdminNotificationController.java`: 공지사항 관리
- `report/controller/ReportAdminController.java`: 신고 관리
- `report/service/ReportAdminService.java`: 신고 관리 서비스

### 권장 구조
```
admin/
  controller/
    AdminController.java (통합)
  service/
    AdminService.java
    AdminServiceImpl.java
  dto/
```

**또는** 각 도메인별 Admin Controller 유지하되, 공통 Admin Service 분리 검토

---

## 18. 주석 형식 통일

### 문제
주석 형식이 일관되지 않음
- `/** */` Javadoc 형식
- `//` 한 줄 주석
- `//` 여러 줄 주석

### 영향 받는 파일
모든 Service, Controller 파일

### 해결 방안
- 공개 메서드는 Javadoc 형식
- 내부 메서드/로직은 `//` 형식
- 주석 내용 형식 통일 (한글/영문 통일)

---

## 19. 매직 넘버 상수화

### 문제
매직 넘버가 하드코딩되어 있음
- `60 * 30` (액세스 토큰 만료 시간)
- `60L * 60 * 24 * 14` (리프레시 토큰 만료 시간)
- `5 * 60 * 1000L` (이메일 인증 TTL)

### 해결 방안
`JwtConfig` 또는 별도 상수 클래스에 정의
```java
public class JwtConstants {
    public static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 60 * 30;
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 60L * 60 * 24 * 14;
    public static final long EMAIL_AUTH_TTL_MS = 5 * 60 * 1000L;
}
```

---

## 20. CustomUserDetails에 nickname 필드 추가 검토

### 문제
`CustomUserDetails`에 `userId`, `email`은 있으나 `nickname`은 없음

### 영향
- Controller에서 사용자 정보가 필요할 때 추가 조회 필요
- `CustomUserDetails`에 nickname 추가 시 중복 조회 방지 가능

### 해결 방안
- JWT 토큰에 nickname 포함 여부 확인
- 포함되어 있다면 `CustomUserDetails`에 nickname 필드 추가 검토
- 성능 영향 고려하여 결정

---

## 참고사항

- 리팩토링 시 기존 테스트 코드 확인 필요
- 리팩토링은 단계적으로 진행하여 기능 동작 보장
- 코드 리뷰를 통해 리팩토링 방향 검토 권장
