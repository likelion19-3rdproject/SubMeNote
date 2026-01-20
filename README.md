# SNS-Service

크리에이터 구독 기반 소셜 네트워크 서비스 (SNS) 플랫폼

## 📋 목차

- [프로젝트 소개](#프로젝트-소개)
- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [설치 및 실행 방법](#설치-및-실행-방법)
- [주요 기능](#주요-기능)
- [DB/ERD](#dberd)
- [API 명세서](#api-명세서)
- [코드 스타일 및 커밋 규칙](#코드-스타일-및-커밋-규칙)

---

## 프로젝트 소개

SNS-Service는 크리에이터와 구독자를 연결하는 구독 기반 소셜 네트워크 서비스 플랫폼입니다. 크리에이터는 유료 구독 서비스를 제공하고, 구독자는 크리에이터의 전용 콘텐츠를 구독하여 소통할 수 있습니다.

### 주요 특징

- 🔐 **JWT 기반 인증/인가 시스템**: 쿠키 기반 토큰 관리로 안전한 사용자 인증
- 👥 **역할 기반 접근 제어**: 일반 사용자, 크리에이터, 관리자 역할 분리
- 💳 **Toss Payments 연동**: 안전하고 간편한 결제 시스템
- 📝 **게시글 관리**: 게시글 작성, 수정, 삭제 및 검색 기능 (제목 기반 검색)
- 💬 **댓글 시스템**: 게시글별 댓글 작성 및 관리
- ❤️ **좋아요 기능**: 게시글 및 댓글에 좋아요 표시
- 🔔 **실시간 알림**: 구독, 좋아요, 댓글 등 다양한 알림 제공
- 🔍 **검색 기능**: 크리에이터 검색, 구독한 크리에이터 게시글 검색
- 🛡️ **신고 시스템**: 게시글, 댓글, 사용자 신고 및 관리자 처리
- 📊 **정산 시스템**: 크리에이터 수익 정산 내역 조회
- 🖼️ **프로필 이미지**: 사용자 프로필 이미지 업로드 및 관리
- 🚀 **자동 배포**: GitHub Actions를 통한 CI/CD 파이프라인

---

## 프로젝트 개요

### 프로젝트 구조

```
SNS-Service/
├── backend/                 # Spring Boot 백엔드
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/backend/
│   │   │   │   ├── auth/          # 인증/인가
│   │   │   │   ├── user/          # 사용자 관리
│   │   │   │   ├── post/          # 게시글
│   │   │   │   ├── comment/       # 댓글
│   │   │   │   ├── subscribe/    # 구독
│   │   │   │   ├── payment/       # 결제
│   │   │   │   ├── order/         # 주문
│   │   │   │   ├── report/        # 신고
│   │   │   │   ├── settlement/    # 정산
│   │   │   │   ├── email/         # 이메일
│   │   │   │   └── global/        # 공통 설정
│   │   │   └── resources/
│   │   └── test/
│   ├── build.gradle
│   ├── Dockerfile.dev
│   └── docker-compose.dev.yml
│
├── front/                   # Next.js 프론트엔드
│   ├── app/                 # Next.js App Router
│   ├── src/
│   │   ├── api/             # API 클라이언트
│   │   ├── components/       # React 컴포넌트
│   │   ├── hooks/           # Custom Hooks
│   │   ├── lib/             # 유틸리티
│   │   └── types/           # TypeScript 타입
│   ├── package.json
│   ├── Dockerfile.dev
│   ├── Dockerfile.prod
│   └── docker-compose.dev.yml
│
├── docker-compose.dev.yml   # 개발 환경 Docker Compose
├── docker-compose.prod.yml  # 프로덕션 환경 Docker Compose
└── README.md                # 프로젝트 README
```

### 아키텍처

- **Backend**: Spring Boot 기반 RESTful API 서버
- **Frontend**: Next.js 기반 SPA (Server-Side Rendering 지원)
- **Database**: MySQL 8.0 (관계형 데이터베이스)
- **Cache**: Redis 7 (세션 및 캐시 관리)
- **Infrastructure**: Docker & Docker Compose를 통한 컨테이너화

---

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.9
- **Language**: Java 21
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle
- **ORM**: Spring Data JPA

### Frontend
- **Framework**: Next.js 16.1.1
- **Language**: TypeScript
- **UI Library**: React 19.2.3
- **Styling**: Tailwind CSS 4
- **HTTP Client**: Axios

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions
- **Cloud**: AWS EC2
- **Payment**: Toss Payments

---

## 설치 및 실행 방법

### 사전 요구사항

- Docker & Docker Compose
- Git

### 로컬 개발 환경 실행

#### 1. 저장소 클론

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/SNS-Service.git
cd SNS-Service
```

> ⚠️ **참고**: `YOUR_GITHUB_USERNAME`을 실제 GitHub 사용자명 또는 조직명으로 변경하세요.

#### 2. 환경 변수 설정

**Backend 환경 변수** (`backend/.env.dev` 파일 생성):

```bash
# MySQL 데이터베이스 설정
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=sns_db
MYSQL_USER=sns_user
MYSQL_PASSWORD=your_db_password

# Spring Boot 데이터베이스 연결
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/sns_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=sns_user
SPRING_DATASOURCE_PASSWORD=your_db_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# Spring Boot JPA 설정
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# JWT 설정 (실제 값으로 변경 필요)
JWT_SECRET=your_jwt_secret_key_minimum_256_bits  # 최소 256비트 이상의 강력한 랜덤 문자열 사용
JWT_ACCESS_TOKEN_MS=1800000  # 30분
JWT_REFRESH_TOKEN_MS=1209600000  # 14일

# Toss Payments (테스트 키 사용)
TOSS_PAYMENTS_SECRET_KEY=test_sk_xxxxxxxxxxxxx

# 이메일 설정 (SMTP) - 실제 이메일 계정 정보로 변경 필요
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@example.com
SPRING_MAIL_PASSWORD=your_app_password

# Redis 설정 (선택사항)
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=
```

**Frontend 환경 변수** (`front/.env.dev` 파일 생성):

```bash
# API URL
NEXT_PUBLIC_API_URL=http://localhost:8080

# Next.js 환경 설정
NODE_ENV=development
PORT=3000

# Toss Payments (테스트 키 사용)
NEXT_PUBLIC_TOSS_CLIENT_KEY=test_ck_xxxxxxxxxxxxx
NEXT_PUBLIC_TOSS_REDIRECT_MODE=frontend
```

#### 3. 백엔드 실행

```bash
cd backend

# 이전에 main에서 실행한 경우 빌드 삭제
rm -rf build
./gradlew clean

# Docker Compose로 실행
docker compose -f docker-compose.dev.yml up -d --build
```

실행 확인: `http://localhost:8080/api/home` 접속 시 크리에이터 목록이 보이면 성공

#### 4. 프론트엔드 실행

```bash
cd front

# Docker Compose로 실행
docker compose -f docker-compose.dev.yml up -d --build
```

실행 확인: `http://localhost:3000` 접속 시 Next.js 홈화면이 보이면 성공

#### 5. 전체 서비스 동시 실행

프로젝트 루트에서:

```bash
docker compose -f docker-compose.dev.yml up -d --build
```

### 서비스 종료

```bash
# 볼륨 포함 삭제
docker compose -f docker-compose.dev.yml down -v

# 볼륨 유지
docker compose -f docker-compose.dev.yml down
```

---

## 주요 기능

### 인증/인가
- 회원가입 (이메일 인증)
- 로그인/로그아웃
- JWT 토큰 기반 인증
- 토큰 갱신 (Refresh Token)

### 사용자 관리
- 일반 사용자
- 크리에이터 (신청 및 승인)
- 관리자

### 크리에이터 기능
- 크리에이터 신청
- 프로필 이미지 업로드 및 관리
- 게시글 작성/수정/삭제
- 게시글 공개 설정 (공개/구독자 전용)
- 정산 내역 조회

### 구독 기능
- 크리에이터 구독/구독 취소
- 구독한 크리에이터 목록 조회
- 구독한 크리에이터 게시글 조회
- 게시글 제목 검색

### 게시글 기능
- 게시글 작성/수정/삭제
- 게시글 목록 조회 (페이징)
- 게시글 상세 조회
- 구독한 크리에이터 게시글 검색

### 댓글 기능
- 댓글 작성/수정/삭제
- 댓글 목록 조회 (페이징)
- 게시글별 댓글 조회

### 결제 기능
- Toss Payments 연동
- 주문 생성 및 조회
- 결제 성공/실패 처리

### 좋아요 기능
- 게시글 좋아요/좋아요 취소
- 댓글 좋아요/좋아요 취소

### 알림 기능
- 실시간 알림 조회
- 알림 읽음 처리
- 관리자 알림 관리

### 관리자 기능
- 크리에이터 신청 승인/거부
- 신고 처리 (게시글/댓글/사용자)
- 정산 관리 및 조회

---

## DB/ERD

### 주요 엔티티

- **User**: 사용자 정보 (일반 사용자, 크리에이터, 관리자)
- **Role**: 사용자 역할 (ROLE_USER, ROLE_CREATOR, ROLE_ADMIN)
- **Post**: 게시글
- **Comment**: 댓글
- **Subscribe**: 구독 관계
- **Like**: 좋아요 (게시글/댓글)
- **Order**: 주문
- **Payment**: 결제
- **Settlement**: 정산
- **Report**: 신고
- **Notification**: 알림

### 주요 관계

- User ↔ Role: 다대다 (Many-to-Many)
- User ↔ Post: 일대다 (One-to-Many)
- User ↔ Subscribe: 다대다 (구독 관계)
- Post ↔ Comment: 일대다 (One-to-Many)
- Post ↔ Like: 다대다 (Many-to-Many)
- User ↔ Order: 일대다 (One-to-Many)
- Order ↔ Payment: 일대일 (One-to-One)

---

## API 명세서

### 인증

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| POST | `/api/auth/signup` | 회원가입 | ❌ |
| POST | `/api/auth/login` | 로그인 | ❌ |
| POST | `/api/auth/logout` | 로그아웃 | ✅ |
| POST | `/api/auth/refresh` | 토큰 갱신 | ❌ |

### 사용자

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/home` | 크리에이터 목록 조회 (페이징) | ❌ |
| GET | `/api/home/search` | 크리에이터 검색 (닉네임, 페이징) | ❌ |
| GET | `/api/users/me` | 내 정보 조회 | ✅ |
| DELETE | `/api/users/me` | 회원 탈퇴 | ✅ |
| POST | `/api/profile-images/me` | 프로필 이미지 업로드 | ✅ |
| GET | `/api/profile-images/users/{userId}` | 프로필 이미지 조회 | ❌ |
| POST | `/api/creators/apply` | 크리에이터 신청 | ✅ |
| GET | `/api/creators/{creatorId}` | 크리에이터 정보 조회 | ❌ |

### 게시글

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/posts` | 구독한 크리에이터 게시글 목록 | ✅ |
| GET | `/api/posts/search` | 구독한 크리에이터 게시글 검색 | ✅ |
| GET | `/api/posts/{postId}` | 게시글 상세 조회 | ✅ |
| POST | `/api/posts` | 게시글 작성 | ✅ (크리에이터) |
| PUT | `/api/posts/{postId}` | 게시글 수정 | ✅ (크리에이터) |
| DELETE | `/api/posts/{postId}` | 게시글 삭제 | ✅ (크리에이터) |

### 구독

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| POST | `/api/subscribes/{creatorId}` | 크리에이터 구독 | ✅ |
| DELETE | `/api/subscribes/{creatorId}` | 구독 취소 | ✅ |
| GET | `/api/subscribes` | 내 구독 목록 조회 | ✅ |

### 결제

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| POST | `/api/orders` | 주문 생성 | ✅ |
| GET | `/api/orders` | 주문 목록 조회 | ✅ |
| GET | `/api/orders/{orderId}` | 주문 상세 조회 | ✅ |
| POST | `/api/payments/confirm` | 결제 확인 | ✅ |
| POST | `/api/payments/fail` | 결제 실패 처리 | ✅ |

### 댓글

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/comments/posts/{postId}` | 게시글 댓글 목록 조회 | ❌ |
| POST | `/api/comments/posts/{postId}` | 댓글 작성 | ✅ |
| PUT | `/api/comments/{commentId}` | 댓글 수정 | ✅ |
| DELETE | `/api/comments/{commentId}` | 댓글 삭제 | ✅ |

### 좋아요

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| POST | `/api/likes/posts/{postId}` | 게시글 좋아요 | ✅ |
| DELETE | `/api/likes/posts/{postId}` | 게시글 좋아요 취소 | ✅ |
| POST | `/api/likes/comments/{commentId}` | 댓글 좋아요 | ✅ |
| DELETE | `/api/likes/comments/{commentId}` | 댓글 좋아요 취소 | ✅ |

### 신고

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| POST | `/api/reports/posts/{postId}` | 게시글 신고 | ✅ |
| POST | `/api/reports/comments/{commentId}` | 댓글 신고 | ✅ |
| POST | `/api/reports/users/{userId}` | 사용자 신고 | ✅ |

### 정산

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/settlements` | 크리에이터 정산 내역 조회 | ✅ (크리에이터) |

### 알림

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/notifications` | 알림 목록 조회 | ✅ |
| PUT | `/api/notifications/{notificationId}` | 알림 읽음 처리 | ✅ |
| GET | `/api/admin/notifications` | 관리자 알림 목록 | ✅ (관리자) |

### 관리자

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/admin/creator-applications` | 크리에이터 신청 목록 | ✅ (관리자) |
| POST | `/api/admin/creator-applications/{applicationId}` | 크리에이터 신청 처리 | ✅ (관리자) |
| GET | `/api/report/admin` | 신고 목록 조회 | ✅ (관리자) |
| POST | `/api/report/admin/{reportId}` | 신고 처리 | ✅ (관리자) |

---

## 코드 스타일 및 커밋 규칙

### 코드 스타일

#### 백엔드 (Java/Spring Boot)

**네이밍 컨벤션**
- 클래스명: `PascalCase` (예: `UserController`, `PostService`)
- 메서드명: `camelCase` (예: `getUserById`, `createPost`)
- 상수: `UPPER_SNAKE_CASE` (예: `MAX_RETRY_COUNT`)
- 패키지명: 소문자, 점으로 구분 (예: `com.backend.user.controller`)

**파일 구조**
```
com.backend.{domain}/
├── controller/     # REST API 엔드포인트
├── service/        # 비즈니스 로직
├── repository/     # 데이터 접근 계층
├── entity/         # JPA 엔티티
├── dto/            # 데이터 전송 객체
└── exception/      # 도메인별 예외
```

**주요 규칙**
- Lombok 사용: `@Getter`, `@Setter`, `@Builder` 등 적극 활용
- 불변 객체: DTO는 `@Builder` 패턴 사용, 엔티티는 setter 최소화
- Null 안전성: `Optional` 사용, `@Nullable`/`@NonNull` 어노테이션 활용
- 의존성 주입: 생성자 주입 사용 (필드 주입 지양)

#### 프론트엔드 (TypeScript/Next.js)

**네이밍 컨벤션**
- 컴포넌트: `PascalCase` (예: `UserProfile`, `PostCard`)
- 함수/변수: `camelCase` (예: `getUserData`, `isLoading`)
- 상수: `UPPER_SNAKE_CASE` (예: `API_BASE_URL`)
- 파일명: 컴포넌트는 `PascalCase.tsx`, 유틸은 `camelCase.ts`

**파일 구조**
```
src/
├── api/            # API 클라이언트
├── components/     # 재사용 가능한 컴포넌트
│   ├── common/     # 공통 컴포넌트
│   └── {feature}/  # 기능별 컴포넌트
├── hooks/          # Custom Hooks
├── lib/            # 유틸리티 함수
└── types/          # TypeScript 타입 정의
```

### 커밋 메시지 컨벤션

**커밋 메시지 형식**
```
# type(이슈 태그): Title(짧은 한 줄)
#
# Body
```

**이슈 태그 종류**
- ✨ **기능**: 새로운 기능 추가 (예: `login` 기능 최초 구현)
- 📜 **문서**: 문서 수정 (예: `doc.md` 파일 수정)
- ⚙️ **설정**: 설정 파일 변경 (예: `.env`, `docker-compose.yml`, `.gitignore` 설정)
- 🐞 **픽스**: 버그 수정 (예: `#<issue-number> Bug` 픽스)
- 🎨 **디자인**: UI/UX 변경 (예: `UserCard` 컴포넌트 UI 디자인 변경)
- 🛠️ **리팩터링**: 코드 리팩토링 (예: `bulkFilesProcessing` 성능 개선)
- 🗑️ **삭제**: 파일/코드 삭제 (예: `delete.class` 파일 삭제)
- ♻️ **변경**: 패키지명 변경, 클래스명 변경 등 (예: 패키지 명 변경, 클래스 명 변경)

**다중 태그 사용**
- 여러 태그를 함께 사용할 수 있습니다 (예: `🎨 디자인 & 🛠️ 리팩터링`)

**작성 규칙**
- **제목**:
  - 영문 기준 50글자 이하
  - 첫 글자는 대문자로 작성
  - 제목 끝에 마침표 사용하지 않음
  - 명령문으로 작성 (과거형 사용하지 않음)
- **본문**:
  - 0-3줄의 짧은 설명 (변경 사항이 적으면 생략 가능)
  - 각 행은 영문 기준 72글자 이하, 한글은 36자 이하
  - "어떻게"보다는 "무엇"과 "왜"에 집중
- **구분**: 제목과 본문은 빈 행으로 구분

**예시**
```bash
# 기능 추가
git commit -m "✨ 기능: Add user profile image upload"

# 버그 수정
git commit -m "🐞 픽스: Fix cookie setting error in login"

# 문서 수정
git commit -m "📜 문서: Update API documentation"

# 리팩토링
git commit -m "🛠️ 리팩터링: Improve post service layer performance"

# 설정 변경
git commit -m "⚙️ 설정: Update docker-compose.yml configuration"

# 다중 태그
git commit -m "🎨 디자인 & 🛠️ 리팩터링: Redesign ShoppingCard component and improve logic"

# 본문 포함
git commit -m "✨ 기능: Add post search functionality

제목 기반 검색 기능 구현
페이징 처리 및 정렬 기능 추가"

# 이슈 번호 포함
git commit -m "🐞 픽스: Fix #123 login authentication error"
```

### 브랜치 전략

**브랜치 종류**
- `main`: 프로덕션 배포 브랜치 (안정 버전, 직접 커밋 금지)
- `dev`: 개발 브랜치 (통합 테스트, 기능 병합)
- `deploy`: 배포 브랜치 (자동 배포 트리거)
- `feature/*`: 기능 개발 브랜치 (예: `feature/user-authentication`)

**브랜치 생성 및 병합 규칙**
```bash
# 기능 개발 브랜치 생성
git checkout -b feature/new-feature dev

# 개발 완료 후 dev 브랜치로 병합
git checkout dev
git merge feature/new-feature
git push origin dev

# dev에서 테스트 완료 후 deploy로 병합 (자동 배포)
git checkout deploy
git merge dev
git push origin deploy
```
