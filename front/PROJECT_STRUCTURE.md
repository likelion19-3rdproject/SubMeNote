# 프로젝트 구조

## 폴더 구조

```
front/
├── app/                          # Next.js App Router
│   ├── layout.tsx               # 루트 레이아웃 (Header 포함)
│   ├── page.tsx                 # 메인 페이지 (/)
│   ├── globals.css              # 전역 스타일
│   ├── login/                   # 로그인 페이지
│   │   └── page.tsx
│   ├── signup/                  # 회원가입 페이지
│   │   └── page.tsx
│   ├── feed/                    # 구독 피드 페이지
│   │   └── page.tsx
│   ├── me/                      # 마이페이지
│   │   ├── page.tsx
│   │   ├── subscriptions/      # 내 구독 목록
│   │   │   └── page.tsx
│   │   ├── comments/           # 내 댓글 목록
│   │   │   └── page.tsx
│   │   ├── orders/             # 주문 내역
│   │   │   └── page.tsx
│   │   ├── posts/              # 내 게시글 (크리에이터)
│   │   │   └── page.tsx
│   │   ├── account/            # 계좌 관리 (크리에이터)
│   │   │   └── page.tsx
│   │   └── settlements/        # 정산 내역 (크리에이터)
│   │       ├── page.tsx
│   │       └── [settlementId]/
│   │           └── page.tsx
│   ├── creators/               # 크리에이터 홈
│   │   └── [creatorId]/
│   │       └── page.tsx
│   ├── posts/                  # 게시글 상세
│   │   └── [postId]/
│   │       └── page.tsx
│   ├── creator/                # 크리에이터 전용
│   │   └── posts/
│   │       └── new/
│   │           └── page.tsx
│   ├── subscribe/              # 멤버십 가입
│   │   └── [creatorId]/
│   │       └── page.tsx
│   └── pay/                    # 결제 페이지
│       ├── success/
│       │   └── page.tsx
│       └── fail/
│           └── page.tsx
├── src/
│   ├── api/                    # API 모듈
│   │   ├── authApi.ts
│   │   ├── emailApi.ts
│   │   ├── homeApi.ts
│   │   ├── postApi.ts
│   │   ├── commentApi.ts
│   │   ├── subscribeApi.ts
│   │   ├── orderApi.ts
│   │   ├── paymentApi.ts
│   │   ├── settlementApi.ts
│   │   └── userApi.ts
│   ├── components/             # 컴포넌트
│   │   └── common/            # 공통 컴포넌트
│   │       ├── Header.tsx
│   │       ├── Pagination.tsx
│   │       ├── LoadingSpinner.tsx
│   │       ├── ErrorState.tsx
│   │       ├── Card.tsx
│   │       ├── List.tsx
│   │       ├── Button.tsx
│   │       ├── Input.tsx
│   │       └── Textarea.tsx
│   ├── hooks/                  # 커스텀 훅
│   │   └── useAuth.ts
│   ├── lib/                    # 라이브러리 설정
│   │   └── axios.ts           # axios 인스턴스 및 인터셉터
│   ├── middleware.ts          # Next.js 미들웨어 (라우트 가드)
│   └── types/                  # TypeScript 타입 정의
│       ├── common.ts
│       ├── auth.ts
│       ├── email.ts
│       ├── home.ts
│       ├── post.ts
│       ├── comment.ts
│       ├── subscribe.ts
│       ├── order.ts
│       ├── payment.ts
│       ├── settlement.ts
│       └── user.ts
├── public/                     # 정적 파일
├── package.json
├── tsconfig.json
├── next.config.mjs
├── .env.local.example          # 환경변수 예제
├── TODO.md                     # TODO 목록
└── PROJECT_STRUCTURE.md        # 이 파일
```

## 생성된 파일 목록

### 설정 파일
- `package.json` - 의존성 관리 (axios, TypeScript 추가)
- `tsconfig.json` - TypeScript 설정
- `next.config.mjs` - Next.js 설정 (환경변수 포함)
- `.env.local.example` - 환경변수 예제

### API 모듈 (`src/api/`)
- `authApi.ts` - 인증 API (로그인, 회원가입, 로그아웃, 닉네임 중복 확인)
- `emailApi.ts` - 이메일 API (전송, 재전송, 인증)
- `homeApi.ts` - 홈 API (크리에이터 목록)
- `postApi.ts` - 게시글 API (조회, 작성, 수정, 삭제)
- `commentApi.ts` - 댓글 API (조회, 작성, 수정, 삭제)
- `subscribeApi.ts` - 구독 API (구독, 구독 취소, 내 구독 목록)
- `orderApi.ts` - 주문 API (주문 생성, 주문 내역 조회)
- `paymentApi.ts` - 결제 API (결제 확인)
- `settlementApi.ts` - 정산 API (정산 내역 조회)
- `userApi.ts` - 사용자 API (내 게시글, 내 댓글, 계좌 관리, 회원탈퇴)

### 타입 정의 (`src/types/`)
- `common.ts` - 공통 타입 (Page<T>, ApiResponse<T>)
- `auth.ts` - 인증 관련 타입
- `email.ts` - 이메일 관련 타입
- `home.ts` - 홈 관련 타입
- `post.ts` - 게시글 관련 타입
- `comment.ts` - 댓글 관련 타입
- `subscribe.ts` - 구독 관련 타입
- `order.ts` - 주문 관련 타입
- `payment.ts` - 결제 관련 타입
- `settlement.ts` - 정산 관련 타입
- `user.ts` - 사용자 관련 타입

### 공통 컴포넌트 (`src/components/common/`)
- `Header.tsx` - 헤더 (로그인 상태에 따른 메뉴 표시)
- `Pagination.tsx` - 페이지네이션 컴포넌트
- `LoadingSpinner.tsx` - 로딩 스피너
- `ErrorState.tsx` - 에러 상태 표시
- `Card.tsx` - 카드 컴포넌트
- `List.tsx` - 리스트 컴포넌트
- `Button.tsx` - 버튼 컴포넌트
- `Input.tsx` - 입력 필드 컴포넌트
- `Textarea.tsx` - 텍스트 영역 컴포넌트

### 라이브러리 설정 (`src/lib/`)
- `axios.ts` - axios 인스턴스 및 인터셉터 (withCredentials, 401 처리)

### 미들웨어 및 훅
- `src/middleware.ts` - Next.js 미들웨어 (라우트 가드)
- `src/hooks/useAuth.ts` - 인증 상태 관리 훅

### 페이지 (`app/`)
- `layout.tsx` - 루트 레이아웃
- `page.tsx` - 메인 페이지
- `login/page.tsx` - 로그인 페이지
- `signup/page.tsx` - 회원가입 페이지
- `feed/page.tsx` - 구독 피드 페이지
- `me/page.tsx` - 마이페이지
- `me/subscriptions/page.tsx` - 내 구독 목록
- `me/comments/page.tsx` - 내 댓글 목록
- `me/orders/page.tsx` - 주문 내역
- `me/posts/page.tsx` - 내 게시글 (크리에이터)
- `me/account/page.tsx` - 계좌 관리 (크리에이터)
- `me/settlements/page.tsx` - 정산 내역 (크리에이터)
- `me/settlements/[settlementId]/page.tsx` - 정산 상세
- `creators/[creatorId]/page.tsx` - 크리에이터 홈
- `posts/[postId]/page.tsx` - 게시글 상세
- `creator/posts/new/page.tsx` - 게시글 작성
- `subscribe/[creatorId]/page.tsx` - 멤버십 가입
- `pay/success/page.tsx` - 결제 성공 페이지
- `pay/fail/page.tsx` - 결제 실패 페이지

### 문서
- `TODO.md` - 백엔드 스펙 부족으로 인한 TODO 목록
- `PROJECT_STRUCTURE.md` - 프로젝트 구조 문서 (이 파일)

## 주요 기능

### 인증/인가
- HttpOnly 쿠키 기반 인증 (accessToken, refreshToken)
- axios withCredentials 자동 설정
- 401 에러 시 자동 로그인 페이지 리다이렉트
- 미들웨어 기반 라우트 가드

### 토스 결제
- 표준결제 SDK 연동
- frontend/backend 모드 지원 (환경변수로 제어)
- 결제 성공/실패 페이지 구현

### 페이지네이션
- 공통 Pagination 컴포넌트
- Page<T> 타입으로 일관된 페이지네이션 처리

### 에러 처리
- axios 인터셉터를 통한 공통 에러 처리
- ErrorState 컴포넌트로 사용자 친화적 에러 표시

## 환경변수 설정

`.env.local` 파일을 생성하고 다음 변수를 설정하세요:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_TOSS_CLIENT_KEY=test_ck_4yKeq5bgrpLAXPeBzqK4rGX0lzW6
NEXT_PUBLIC_TOSS_REDIRECT_MODE=frontend
```

## 실행 방법

```bash
npm install
npm run dev
```

## 참고사항

- 모든 API는 `/src/api/*`로 모듈화되어 있습니다.
- 타입은 `/src/types/*`에 정의되어 있습니다.
- 공통 컴포넌트는 `/src/components/common/*`에 있습니다.
- 백엔드 스펙 부족으로 인한 TODO는 `TODO.md`를 참고하세요.

