# 백엔드 스펙/기능 부족으로 프론트에서 막힌 TODO

## 인증 관련

1. **사용자 인증 상태 확인 API 부재**

   - 현재: 쿠키 존재 여부로만 인증 상태 판단
   - 필요: `GET /api/users/me` 같은 인증 상태 확인 API
   - 영향: Header 컴포넌트에서 로그인 상태 및 크리에이터 여부 확인 불가
   - 위치: `src/components/common/Header.tsx`, `src/hooks/useAuth.ts`

2. **사용자 정보 조회 API 부재**
   - 현재: 사용자 닉네임, 역할(role) 정보를 가져올 수 없음
   - 필요: `GET /api/users/me` 응답에 사용자 정보 포함
   - 영향: 마이페이지에서 사용자 정보 표시 불가

## 구독 관련

3. **크리에이터별 구독 상태 확인 API 부재**
   - 현재: 전체 구독 목록을 가져와서 찾는 방식
   - 필요: `GET /api/subscribes/creators/{creatorId}` 같은 특정 크리에이터 구독 상태 확인 API
   - 영향: 크리에이터 페이지에서 구독 상태 확인이 비효율적
   - 위치: `app/creators/[creatorId]/page.tsx`

## 계좌 관리

4. **계좌 정보 조회 API 부재**
   - 현재: 기존 계좌 정보를 불러올 수 없음
   - 필요: `GET /api/users/me/account` API
   - 영향: 계좌 관리 페이지에서 기존 계좌 정보 표시 및 수정/등록 분기 불가
   - 위치: `app/me/account/page.tsx`

## 결제 관련

5. **결제 시 사용자 정보 필요**
   - 현재: customerEmail, customerName을 빈 문자열로 전송
   - 필요: 사용자 이메일, 이름 정보
   - 영향: 결제 요청 시 사용자 정보 미입력
   - 위치: `app/subscribe/[creatorId]/page.tsx`

## 프로필 관련

6. **크리에이터 프로필 정보 API 부재**
   - 현재: 크리에이터 ID만 표시
   - 필요: `GET /api/creators/{creatorId}` 또는 크리에이터 프로필 정보 API
   - 영향: 크리에이터 페이지에서 프로필 정보 표시 불가
   - 위치: `app/creators/[creatorId]/page.tsx`

## 댓글 관련

7. **댓글 수정 기능 UI 미구현**
   - 현재: 댓글 삭제만 가능
   - 필요: 댓글 수정 UI 및 로직
   - 참고: API는 `PATCH /api/comments/{commentId}` 존재
   - 위치: `app/posts/[postId]/page.tsx`

## 게시글 관련

8. **게시글 수정/삭제 기능 UI 미구현**
   - 현재: 게시글 작성만 가능
   - 필요: 게시글 수정/삭제 UI 및 로직
   - 참고: API는 `PATCH /api/posts/{postId}`, `DELETE /api/posts/{postId}` 존재
   - 위치: `app/posts/[postId]/page.tsx`, `app/me/posts/page.tsx`

## 멤버십 관련

12. **멤버십 해지/철회 기능 구현 완료**
    - 구현: `PATCH /api/subscribes/{subscribeId}` API를 사용하여 status를 CANCELED/ACTIVE로 변경
    - 멤버십 해지: status를 CANCELED로 변경
    - 멤버십 해지 철회: status를 ACTIVE로 변경
    - 위치: `app/creators/[creatorId]/page.tsx`

## 정산 관련

13. **정산내역 월별 조회 API 부재**
    - 현재: 프론트에서 클라이언트 사이드 필터링 (임시)
    - 필요: `GET /api/settlements?page&size&months` 같은 월별 조회 파라미터
    - 영향: 정산내역 페이지에서 월별 조회가 비효율적 (모든 데이터를 가져온 후 필터링)
    - 위치: `app/me/settlements/page.tsx`

## 기타

14. **에러 응답 형식 통일 필요**

    - 현재: 백엔드 에러 응답 형식이 일관되지 않을 수 있음
    - 필요: 표준화된 에러 응답 형식 확인
    - 영향: 에러 메시지 표시가 일관되지 않을 수 있음

15. **페이지네이션 파라미터 확인**

    - 현재: `page`, `size` 파라미터 사용
    - 필요: 백엔드 API의 실제 페이지네이션 파라미터 확인 (0-based vs 1-based 등)

16. **환경변수 설정**
    - 현재: `.env.local.example` 파일 생성
    - 필요: 실제 `.env.local` 파일 생성 및 환경변수 설정
    - 위치: 프로젝트 루트

## 구현 완료된 기능

- ✅ 로그인/회원가입
- ✅ 이메일 인증
- ✅ 크리에이터 목록 조회
- ✅ 게시글 조회/작성
- ✅ 댓글 조회/작성/삭제
- ✅ 구독 기능
- ✅ 멤버십 해지/철회 기능
- ✅ 주문 내역 조회
- ✅ 정산 내역 조회
- ✅ 토스 결제 연동 (frontend/backend 모드 지원)
- ✅ 라우트 가드 (middleware)
- ✅ axios 인터셉터 (401 처리)
