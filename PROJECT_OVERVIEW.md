# 프로젝트 개요

## 1. 프로젝트 한 줄 설명

**크리에이터 구독 기반 소셜 네트워크 서비스 (SNS-Service)**

사용자가 크리에이터를 구독하고, 유료 멤버십을 통해 독점 콘텐츠에 접근할 수 있는 구독형 소셜 네트워크 플랫폼입니다.

---

## 2. 사용자 역할

### 👤 일반 사용자 (ROLE_USER)
- **기본 기능**
  - 회원가입 및 로그인
  - 크리에이터 프로필 조회
  - 공개 게시글 조회 및 피드 확인
  - 댓글 작성 및 좋아요
  - 크리에이터 무료 구독
  - 크리에이터 신청

### 🎨 크리에이터 (ROLE_CREATOR)
- **기본 기능** (일반 사용자 기능 포함)
  - 게시글 작성 및 관리
  - 공개/구독자 전용 게시글 설정
  - 유료 멤버십 판매 (1개월, 3개월, 12개월)
  - 정산 내역 조회
  - 프로필 이미지 관리

### 👑 관리자 (ROLE_ADMIN)
- **기본 기능** (모든 기능 접근 가능)
  - 사용자 및 크리에이터 관리
  - 크리에이터 신청 승인/거부
  - 게시글 및 댓글 신고 처리
  - 공지사항 작성 및 관리
  - 전체 시스템 모니터링

---

## 3. 프로젝트의 목적 및 포인트

### 🎯 프로젝트 목적
크리에이터가 자신의 콘텐츠를 통해 수익을 창출할 수 있고, 사용자는 구독을 통해 원하는 크리에이터의 독점 콘텐츠에 접근할 수 있는 **구독형 콘텐츠 플랫폼**을 구축하는 것입니다.

### 💡 주요 포인트

#### 1. **구독 기반 수익 모델**
   - 무료 구독: 기본 콘텐츠 접근
   - 유료 멤버십: 독점 콘텐츠 접근 (1개월/3개월/12개월)
   - 토스페이먼츠 연동을 통한 안전한 결제 시스템

#### 2. **자동화된 정산 시스템**
   - 주간 원장 기록: 매주 월요일 자동 실행
   - 월간 정산 확정: 매월 1일 자동 실행
   - 플랫폼 수수료 10%, 크리에이터 정산 90%
   - 배치 작업을 통한 정산 프로세스 자동화

#### 3. **콘텐츠 접근 제어**
   - 공개 게시글: 모든 사용자 접근 가능
   - 구독자 전용 게시글: 구독자만 접근 가능
   - 유료 멤버십 전용 콘텐츠 지원 (향후 확장 가능)

#### 4. **안전한 인증 시스템**
   - JWT 기반 인증
   - Refresh Token 자동 갱신
   - 역할 기반 접근 제어 (RBAC)
   - Redis를 활용한 세션 관리

#### 5. **실시간 알림 시스템**
   - 댓글 알림
   - 좋아요 알림
   - 구독 알림
   - 관리자 공지사항 알림

#### 6. **커뮤니티 관리**
   - 게시글/댓글 신고 기능
   - 관리자 신고 처리 시스템
   - 사용자 및 크리에이터 관리

---

## 4. 주요 기능 목록

### 🔐 인증 및 사용자 관리
- [x] 회원가입 / 로그인 / 로그아웃
- [x] JWT 기반 인증 (Access Token + Refresh Token)
- [x] 이메일 인증
- [x] 프로필 관리
- [x] 크리에이터 신청 및 승인

### 📝 콘텐츠 관리
- [x] 게시글 작성 / 수정 / 삭제
- [x] 게시글 조회 (공개 / 구독자 전용)
- [x] 게시글 검색
- [x] 댓글 작성 / 수정 / 삭제
- [x] 게시글 좋아요

### 👥 구독 및 멤버십
- [x] 크리에이터 무료 구독
- [x] 유료 멤버십 구매 (1개월)
- [x] 구독 목록 조회
- [x] 구독 취소
- [x] 멤버십 갱신

### 💳 결제 시스템
- [x] 주문 생성
- [x] 토스페이먼츠 결제 연동
- [x] 결제 성공 / 실패 처리
- [x] 주문 내역 조회
- [x] 결제 취소 처리

### 💰 정산 시스템
- [x] 정산 원장 자동 기록 (주간 배치)
- [x] 월간 정산 자동 확정 (월간 배치)
- [x] 정산 내역 조회
- [x] 대기 중인 정산 조회
- [x] 정산 상세 조회

### 🔔 알림 시스템
- [x] 실시간 알림 조회
- [x] 알림 읽음 처리
- [x] 알림 타입별 필터링
- [x] 알림 삭제

### 🛡️ 신고 및 관리
- [x] 게시글 신고
- [x] 댓글 신고
- [x] 관리자 신고 처리
- [x] 사용자 관리
- [x] 크리에이터 관리
- [x] 공지사항 작성

### 📊 관리자 대시보드
- [x] 사용자 통계
- [x] 크리에이터 통계
- [x] 크리에이터 신청 관리
- [x] 신고 내역 관리

---

## 5. 기술 스택

### 🖥️ Backend
| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.5.9 |
| **Language** | Java 21 |
| **Build Tool** | Gradle |
| **Security** | Spring Security + JWT (jjwt 0.12.5) |
| **ORM** | Spring Data JPA |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 |
| **Email** | Spring Mail |
| **Batch** | Spring Batch |
| **Validation** | Spring Validation |

### 🎨 Frontend
| 분류 | 기술 |
|------|------|
| **Framework** | Next.js 16.1.1 |
| **Language** | TypeScript 5 |
| **UI Library** | React 19.2.3 |
| **Styling** | Tailwind CSS 4 |
| **HTTP Client** | Axios 1.7.9 |
| **Routing** | App Router |

### 🏗️ Infrastructure
| 분류 | 기술 |
|------|------|
| **Containerization** | Docker |
| **Orchestration** | Docker Compose |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 |

### 🔌 External Services
| 서비스 | 용도 |
|--------|------|
| **토스페이먼츠** | 결제 처리 |
| **SMTP 서버** | 이메일 발송 |

---

## 프로젝트 구조

```
secondlionproject-team3/
├── backend/          # Spring Boot 백엔드
│   ├── src/
│   │   └── main/
│   │       └── java/com/backend/
│   │           ├── auth/          # 인증/인가
│   │           ├── user/          # 사용자 관리
│   │           ├── post/         # 게시글
│   │           ├── comment/      # 댓글
│   │           ├── like/         # 좋아요
│   │           ├── subscribe/    # 구독
│   │           ├── order/        # 주문
│   │           ├── payment/      # 결제
│   │           ├── settlement/  # 정산
│   │           ├── notification/ # 알림
│   │           ├── report/       # 신고
│   │           ├── admin/        # 관리자
│   │           └── global/       # 공통 설정
│   └── build.gradle
│
├── front/           # Next.js 프론트엔드
│   ├── app/         # App Router 페이지
│   ├── src/
│   │   ├── api/     # API 클라이언트
│   │   ├── components/ # React 컴포넌트
│   │   ├── hooks/   # Custom Hooks
│   │   ├── lib/     # 유틸리티
│   │   └── types/   # TypeScript 타입
│   └── package.json
│
└── docker-compose.dev.yml  # 개발 환경 설정
```

---

## 실행 방법

### 전체 실행 (Docker Compose)
```bash
docker compose -f docker-compose.dev.yml up -d --build
```

### 개별 실행

#### Backend
```bash
cd backend
docker compose -f docker-compose.dev.yml up -d --build
# 접속: http://localhost:8080
```

#### Frontend
```bash
cd front
docker compose -f docker-compose.dev.yml up -d --build
# 접속: http://localhost:3000
```

### 종료
```bash
docker compose -f docker-compose.dev.yml down -v
```

---

## 관련 문서

- [시스템 아키텍처](./ARCHITECTURE.md) - 전체 시스템 구조 및 아키텍처
- [결제 및 구독 플로우](./PAYMENT_SUBSCRIPTION_FLOW.md) - 결제 및 구독 프로세스 상세
- [정산 플로우](./SETTLEMENT_FLOW.md) - 정산 시스템 프로세스 상세


