# 결제 및 구독 플로우 문서

## 개요
이 문서는 SNS-Service의 결제 및 구독 시스템의 전체 플로우를 설명합니다.
- **무료 구독**: 사용자가 크리에이터를 무료로 구독
- **유료 구독**: 결제를 통한 멤버십 구독
- **구독 갱신**: 기존 구독 기간 연장
- **결제 처리**: 토스페이먼츠 연동을 통한 결제 승인 및 실패 처리

---

## 무료 구독 플로우

```mermaid
sequenceDiagram
    participant User as 사용자<br/>(Frontend)
    participant Frontend as Next.js<br/>페이지
    participant Backend as Spring Boot<br/>API
    participant SubscribeSvc as SubscribeService
    participant DB as MySQL<br/>Database
    
    Note over User,DB: 무료 구독 프로세스
    User->>Frontend: 크리에이터 페이지 접속<br/>구독 버튼 클릭
    Frontend->>Backend: POST /api/subscribes/{creatorId}
    Backend->>SubscribeSvc: createSubscribe(userId, creatorId)
    
    SubscribeSvc->>DB: 사용자 조회
    DB-->>SubscribeSvc: User 정보
    SubscribeSvc->>DB: 크리에이터 조회
    DB-->>SubscribeSvc: Creator 정보
    
    Note over SubscribeSvc: 검증<br/>1. 크리에이터 역할 확인<br/>2. 자기 자신 구독 방지
    
    SubscribeSvc->>DB: Subscribe 생성<br/>(status: ACTIVE, type: FREE, expiredAt: null)
    DB-->>SubscribeSvc: Subscribe 저장 완료
    SubscribeSvc-->>Backend: SubscribeResponseDto
    Backend-->>Frontend: 201 Created
    Frontend-->>User: 구독 완료 메시지
```

---

## 유료 구독(결제) 플로우 - 성공 케이스

```mermaid
sequenceDiagram
    participant User as 사용자<br/>(Frontend)
    participant Frontend as Next.js<br/>Subscribe Page
    participant Backend as Spring Boot<br/>API
    participant OrderSvc as OrderService
    participant TossSDK as Toss Payments<br/>SDK
    participant TossAPI as Toss Payments<br/>API
    participant PaymentFacade as PaymentFacade
    participant PaymentSvc as PaymentService
    participant SubscribeSvc as SubscribeService
    participant DB as MySQL<br/>Database
    
    Note over User,DB: 1단계: 주문 생성
    User->>Frontend: 구독 기간 선택<br/>(1개월, 3개월, 12개월)
    User->>Frontend: 결제 버튼 클릭
    Frontend->>Backend: POST /api/orders<br/>{creatorId, orderName, amount}
    Backend->>OrderSvc: createOrder(userId, creatorId, orderName, amount)
    
    OrderSvc->>DB: 사용자 및 크리에이터 조회
    DB-->>OrderSvc: User, Creator 정보
    Note over OrderSvc: 검증<br/>1. 크리에이터 역할 확인<br/>2. 자기 자신 구독 방지
    
    OrderSvc->>DB: Order 생성<br/>(orderId: UUID, status: PENDING, expiredAt: +30분)
    DB-->>OrderSvc: Order 저장 완료
    OrderSvc-->>Backend: OrderCreateResponseDto<br/>(orderId, orderName, amount)
    Backend-->>Frontend: 201 Created
    
    Note over User,DB: 2단계: 결제 요청
    Frontend->>TossSDK: payment.requestPayment()<br/>{method: CARD, amount, orderId, orderName}
    TossSDK->>User: 결제창 표시
    User->>TossSDK: 결제 정보 입력<br/>(카드번호, CVC 등)
    TossSDK->>TossAPI: 결제 승인 요청
    TossAPI-->>TossSDK: 결제 승인 완료<br/>(paymentKey 반환)
    TossSDK-->>Frontend: successUrl로 리다이렉트<br/>(paymentKey, orderId, amount)
    
    Note over User,DB: 3단계: 결제 확인 및 처리
    Frontend->>Frontend: /pay/success 페이지 로드
    Frontend->>Backend: POST /api/payments/confirm<br/>{paymentKey, orderId, amount}
    Backend->>PaymentFacade: confirmPayment(request)
    
    PaymentFacade->>TossAPI: 결제 승인 확인 요청<br/>(paymentKey, orderId, amount)
    TossAPI-->>PaymentFacade: TossPaymentResponse<br/>(paymentKey, totalAmount, approvedAt, method)
    
    PaymentFacade->>PaymentSvc: processPaymentSuccess(request, tossResponse)
    
    PaymentSvc->>DB: Order 조회 (orderId)
    DB-->>PaymentSvc: Order 정보
    Note over PaymentSvc: 검증<br/>1. 이미 결제된 주문인지 확인<br/>2. 금액 일치 확인
    
    PaymentSvc->>DB: Payment 생성<br/>(paymentKey, amount, status: PAID)
    PaymentSvc->>DB: Order 상태 변경<br/>(status: PENDING → PAID)
    
    PaymentSvc->>SubscribeSvc: renewMembership(userId, creatorId, 1)
    
    alt 기존 구독이 있는 경우
        SubscribeSvc->>DB: 기존 Subscribe 조회
        DB-->>SubscribeSvc: Subscribe 정보
        Note over SubscribeSvc: 만료일이 미래인 경우<br/>기존 만료일에서 연장
        SubscribeSvc->>DB: Subscribe 갱신<br/>(type: PAID, expiredAt: 기존+1개월)
    else 기존 구독이 없는 경우
        SubscribeSvc->>DB: Subscribe 생성<br/>(status: ACTIVE, type: PAID, expiredAt: 오늘+1개월)
    end
    
    DB-->>SubscribeSvc: Subscribe 저장 완료
    SubscribeSvc-->>PaymentSvc: 갱신 완료
    PaymentSvc-->>PaymentFacade: PaymentResponse
    PaymentFacade-->>Backend: PaymentResponse
    Backend-->>Frontend: 200 OK
    Frontend-->>User: 결제 완료 메시지<br/>구독 목록으로 이동
```

---

## 유료 구독(결제) 플로우 - 실패 케이스

```mermaid
sequenceDiagram
    participant User as 사용자<br/>(Frontend)
    participant Frontend as Next.js<br/>Subscribe Page
    participant TossSDK as Toss Payments<br/>SDK
    participant TossAPI as Toss Payments<br/>API
    participant Backend as Spring Boot<br/>API
    participant PaymentSvc as PaymentService
    participant DB as MySQL<br/>Database
    
    Note over User,DB: 케이스 1: 사용자가 결제창 닫기
    User->>Frontend: 결제 버튼 클릭
    Frontend->>TossSDK: payment.requestPayment()
    TossSDK->>User: 결제창 표시
    User->>TossSDK: 결제창 닫기 (X 버튼)
    TossSDK-->>Frontend: failUrl로 리다이렉트<br/>(code: USER_CANCEL, orderId)
    Frontend->>Frontend: /pay/fail 페이지 로드
    Frontend->>Backend: POST /api/payments/fail<br/>{orderId, code: USER_CANCEL, message}
    Backend->>PaymentSvc: failPayment(orderId, code)
    PaymentSvc->>DB: Order 조회
    DB-->>PaymentSvc: Order 정보
    PaymentSvc->>DB: Order 상태 변경<br/>(status: PENDING → CANCELED)
    DB-->>PaymentSvc: 저장 완료
    PaymentSvc-->>Backend: 처리 완료
    Backend-->>Frontend: 200 OK
    Frontend-->>User: 결제 취소 메시지
    
    Note over User,DB: 케이스 2: 결제 실패 (잔액 부족, 카드사 거절 등)
    User->>TossSDK: 결제 정보 입력
    TossSDK->>TossAPI: 결제 승인 요청
    TossAPI-->>TossSDK: 결제 실패<br/>(code: PAY_PROCESS_ABORTED 등)
    TossSDK-->>Frontend: failUrl로 리다이렉트<br/>(code, message, orderId)
    Frontend->>Frontend: /pay/fail 페이지 로드
    Frontend->>Backend: POST /api/payments/fail<br/>{orderId, code, message}
    Backend->>PaymentSvc: failPayment(orderId, code)
    PaymentSvc->>DB: Order 조회
    DB-->>PaymentSvc: Order 정보
    PaymentSvc->>DB: Order 상태 변경<br/>(status: PENDING → FAILED)
    DB-->>PaymentSvc: 저장 완료
    PaymentSvc-->>Backend: 처리 완료
    Backend-->>Frontend: 200 OK
    Frontend-->>User: 결제 실패 메시지<br/>(에러 코드 및 메시지 표시)
    
    Note over User,DB: 케이스 3: 결제 확인 중 서버 오류
    Frontend->>Backend: POST /api/payments/confirm
    Backend->>PaymentFacade: confirmPayment(request)
    PaymentFacade->>TossAPI: 결제 승인 확인 요청
    TossAPI-->>PaymentFacade: TossPaymentResponse
    PaymentFacade->>PaymentSvc: processPaymentSuccess()
    PaymentSvc->>DB: Payment 저장 시도
    DB-->>PaymentSvc: 저장 실패 (예외 발생)
    PaymentSvc-->>PaymentFacade: 예외 전파
    PaymentFacade->>TossAPI: 결제 취소 요청<br/>(보상 트랜잭션)
    TossAPI-->>PaymentFacade: 취소 완료
    PaymentFacade-->>Backend: 예외 전파
    Backend-->>Frontend: 500 Internal Server Error
    Frontend->>Frontend: /pay/fail 페이지로 리다이렉트
    Frontend-->>User: 결제 확인 실패 메시지
```

---

## 구독 상태 관리 플로우

```mermaid
stateDiagram-v2
    [*] --> 무료구독: POST /api/subscribes/{creatorId}
    [*] --> 유료구독: 결제 완료 후 renewMembership
    
    무료구독: Subscribe<br/>status: ACTIVE<br/>type: FREE<br/>expiredAt: null
    
    유료구독: Subscribe<br/>status: ACTIVE<br/>type: PAID<br/>expiredAt: 미래 날짜
    
    무료구독 --> 구독취소: PATCH /api/subscribes/{id}<br/>status: CANCELED
    유료구독 --> 구독취소: PATCH /api/subscribes/{id}<br/>status: CANCELED
    
    유료구독 --> 유료구독: 결제 갱신<br/>renewMembership()<br/>만료일 연장
    
    구독취소: Subscribe<br/>status: CANCELED
    
    구독취소 --> [*]: DELETE /api/subscribes/{id}<br/>(만료된 경우만)
    
    note right of 무료구독
        무료 구독은 만료일이 없음
        취소 시 즉시 접근 불가
    end note
    
    note right of 유료구독
        유료 구독은 만료일까지 접근 가능
        만료일 전 갱신 시 기존 만료일에서 연장
        만료일 후에는 접근 불가
    end note
```

---

## 주문 상태 전이도

```mermaid
stateDiagram-v2
    [*] --> PENDING: 주문 생성<br/>createOrder()
    
    PENDING --> IN_PROGRESS: 결제 요청 시작<br/>Toss SDK 호출
    
    PENDING --> CANCELED: 사용자 취소<br/>failPayment(USER_CANCEL)
    PENDING --> EXPIRED: 30분 경과<br/>(만료 처리)
    
    IN_PROGRESS --> PAID: 결제 승인 완료<br/>processPaymentSuccess()
    IN_PROGRESS --> FAILED: 결제 실패<br/>failPayment(기타 코드)
    IN_PROGRESS --> CANCELED: 사용자 취소<br/>failPayment(USER_CANCEL)
    
    PAID --> [*]: 결제 완료<br/>구독 활성화
    
    CANCELED --> [*]: 주문 취소 완료
    FAILED --> [*]: 결제 실패 완료
    EXPIRED --> [*]: 주문 만료 완료
    
    note right of PENDING
        주문 생성 시점
        expiredAt: 현재 + 30분
    end note
    
    note right of PAID
        결제 완료 시점
        - Payment 엔티티 생성
        - Subscribe 갱신
    end note
```

---

## 구독 갱신 로직 상세

```mermaid
flowchart TD
    Start([renewMembership 호출]) --> ValidateUser{사용자 및<br/>크리에이터 검증}
    ValidateUser -->|검증 실패| Error1[예외 발생]
    ValidateUser -->|검증 성공| FindSubscribe{기존 구독<br/>조회}
    
    FindSubscribe -->|구독 없음| Error2[예외 발생<br/>NOT_FOUND_SUBSCRIBE]
    FindSubscribe -->|구독 있음| CheckExpired{만료일 확인}
    
    CheckExpired -->|만료일 없음<br/>또는<br/>이미 만료됨| SetNewExpiry[새 만료일 설정<br/>현재 + months]
    CheckExpired -->|만료일이<br/>미래임| ExtendExpiry[기존 만료일에서<br/>연장: expiredAt + months]
    
    SetNewExpiry --> Activate[구독 활성화<br/>status: ACTIVE]
    ExtendExpiry --> Activate
    
    Activate --> ChangeToPaid[구독 타입 변경<br/>type: PAID]
    ChangeToPaid --> Save[DB 저장]
    Save --> End([완료])
    
    Error1 --> End
    Error2 --> End
    
    style Start fill:#e1f5ff
    style End fill:#c8e6c9
    style Error1 fill:#ffcdd2
    style Error2 fill:#ffcdd2
```

---

## 결제 승인 프로세스 상세

```mermaid
flowchart TD
    Start([결제 확인 요청]) --> Facade[PaymentFacade.confirmPayment]
    Facade --> TossAPI[토스페이먼츠 API<br/>결제 승인 확인]
    
    TossAPI -->|승인 성공| TossResponse[TossPaymentResponse<br/>받음]
    TossAPI -->|승인 실패| TossError[예외 발생]
    
    TossResponse --> ProcessPayment[PaymentService<br/>processPaymentSuccess]
    
    ProcessPayment --> FindOrder[Order 조회]
    FindOrder -->|주문 없음| OrderError[예외 발생<br/>ORDER_NOT_FOUND]
    FindOrder -->|주문 있음| ValidateOrder{주문 검증}
    
    ValidateOrder -->|이미 결제됨| AlreadyPaid[예외 발생<br/>ALREADY_PAID]
    ValidateOrder -->|금액 불일치| AmountMismatch[예외 발생<br/>PAYMENT_AMOUNT_MISMATCH]
    ValidateOrder -->|검증 통과| CreatePayment[Payment 엔티티 생성]
    
    CreatePayment --> SavePayment[Payment 저장]
    SavePayment --> UpdateOrder[Order 상태 변경<br/>PENDING → PAID]
    UpdateOrder --> RenewSubscribe[SubscribeService<br/>renewMembership 호출]
    
    RenewSubscribe -->|성공| Success[PaymentResponse 반환]
    RenewSubscribe -->|실패| DBError[예외 발생]
    
    DBError --> Compensate[보상 트랜잭션<br/>토스페이먼츠 결제 취소]
    Compensate --> Rethrow[예외 재전파]
    
    Success --> End([결제 완료])
    TossError --> End
    OrderError --> End
    AlreadyPaid --> End
    AmountMismatch --> End
    Rethrow --> End
    
    style Start fill:#e1f5ff
    style End fill:#c8e6c9
    style TossError fill:#ffcdd2
    style OrderError fill:#ffcdd2
    style AlreadyPaid fill:#ffcdd2
    style AmountMismatch fill:#ffcdd2
    style DBError fill:#ffcdd2
    style Compensate fill:#fff9c4
```

---

## 데이터 모델 관계도

```mermaid
erDiagram
    USER ||--o{ SUBSCRIBE : "구독"
    USER ||--o{ ORDER : "주문"
    USER ||--o{ PAYMENT : "결제"
    
    SUBSCRIBE {
        bigint id PK
        bigint user_id FK
        bigint creator_id FK
        enum status "ACTIVE, CANCELED"
        enum type "FREE, PAID"
        date expiredAt "null이면 무료 구독"
        datetime created_at
    }
    
    ORDER {
        bigint id PK
        string orderId UK "UUID 기반"
        bigint user_id FK
        bigint creator_id FK
        string orderName
        bigint amount
        enum status "PENDING, IN_PROGRESS, PAID, CANCELED, FAILED, EXPIRED"
        datetime expiredAt "주문 만료 시간"
        datetime created_at
    }
    
    PAYMENT {
        bigint id PK
        string orderId FK
        string paymentKey UK "토스페이먼츠 paymentKey"
        bigint user_id FK
        bigint creator_id FK
        bigint amount
        enum status "PAID, CANCELED"
        datetime paidAt
        datetime created_at
    }
    
    SUBSCRIBE ||--o{ ORDER : "구독 주문"
    ORDER ||--|| PAYMENT : "결제"
    
    note right of SUBSCRIBE
        무료 구독: type=FREE, expiredAt=null
        유료 구독: type=PAID, expiredAt=미래 날짜
        만료일이 지나면 접근 불가
    end note
    
    note right of ORDER
        주문 생성 시 30분 만료 시간 설정
        PENDING 상태에서 결제 진행
        결제 완료 시 PAID로 변경
    end note
    
    note right of PAYMENT
        토스페이먼츠 결제 승인 후 생성
        paymentKey는 토스페이먼츠에서 발급
        결제 취소 시에도 기록 보관
    end note
```

---

## 주요 API 엔드포인트

### 구독 관련
- `POST /api/subscribes/{creatorId}` - 무료 구독 생성
- `PATCH /api/subscribes/{subscribeId}` - 구독 상태 변경 (ACTIVE/CANCELED)
- `DELETE /api/subscribes/{subscribeId}` - 구독 삭제 (만료된 경우만)
- `GET /api/subscribes/my-creator` - 내 구독 목록 조회
- `PATCH /api/subscribes/membership/renew` - 구독 갱신 (수동)

### 주문 관련
- `POST /api/orders` - 주문 생성
- `GET /api/orders` - 주문 목록 조회
- `GET /api/orders/{orderId}` - 주문 상세 조회
- `PATCH /api/orders/{orderId}` - 주문 상태 수정

### 결제 관련
- `POST /api/payments/confirm` - 결제 승인 확인
- `POST /api/payments/fail` - 결제 실패 처리

---

## 주요 비즈니스 로직

### 1. 구독 생성 검증
- 크리에이터 역할 확인 (ROLE_CREATOR)
- 자기 자신 구독 방지
- 중복 구독 허용 (갱신 로직으로 처리)

### 2. 주문 생성 검증
- 크리에이터 역할 확인
- 자기 자신 구독 방지
- 주문 ID는 UUID 기반 고유값 생성
- 주문 만료 시간: 생성 시점 + 30분

### 3. 결제 검증
- 주문 존재 여부 확인
- 중복 결제 방지 (이미 PAID 상태인지 확인)
- 금액 일치 확인 (주문 금액 vs 결제 금액)

### 4. 구독 갱신 로직
- 기존 구독이 있는 경우:
  - 만료일이 미래인 경우: 기존 만료일에서 연장
  - 만료일이 없거나 과거인 경우: 현재 날짜 기준으로 새로 설정
- 기존 구독이 없는 경우: 예외 발생 (먼저 구독 생성 필요)

### 5. 보상 트랜잭션
- 결제 승인은 완료되었으나 DB 저장 실패 시
- 토스페이먼츠 API를 통해 자동 취소 처리
- 데이터 일관성 보장

---

## 에러 처리

### 주문 관련 에러
- `ORDER_NOT_FOUND`: 주문을 찾을 수 없음
- `ORDER_ACCESS_DENIED`: 본인의 주문이 아님

### 결제 관련 에러
- `ALREADY_PAID`: 이미 결제된 주문
- `PAYMENT_AMOUNT_MISMATCH`: 결제 금액 불일치
- `PAY_PROCESS_CANCELED`: 사용자가 결제 취소
- `USER_CANCEL`: 사용자가 결제창 닫기

### 구독 관련 에러
- `NOT_CREATOR`: 크리에이터가 아님
- `CANNOT_SUBSCRIBE_SELF`: 자기 자신 구독 불가
- `NOT_FOUND_SUBSCRIBE`: 구독 정보 없음
- `FORBIDDEN_SUBSCRIBE`: 구독 접근 권한 없음 (만료 또는 취소)
- `CANNOT_DELETE_NOT_EXPIRED`: 만료되지 않은 구독 삭제 불가


