# 정산 플로우 문서

## 개요
이 문서는 SNS-Service의 크리에이터 정산 시스템의 전체 플로우를 설명합니다.
- **정산 원장 기록**: 결제 완료 후 정산 항목(SettlementItem) 생성
- **주간 원장 기록**: 매주 월요일 배치 작업으로 지난주 결제건 기록
- **월간 정산 확정**: 매월 1일 배치 작업으로 전월 정산 확정
- **정산 조회**: 크리에이터가 자신의 정산 내역 조회

---

## 정산 시스템 전체 플로우

```mermaid
graph TB
    Start([결제 완료]) --> Payment[Payment 엔티티<br/>status: PAID]
    
    Payment --> RecordLedger[원장 기록<br/>SettlementItem 생성]
    RecordLedger --> SettlementItem1[SettlementItem<br/>status: RECORDED<br/>settlement_id: null]
    
    SettlementItem1 --> WeeklyBatch[주간 배치 작업<br/>매주 월요일 00:10]
    WeeklyBatch --> SettlementItem2[SettlementItem<br/>status: RECORDED<br/>settlement_id: null]
    
    SettlementItem2 --> MonthlyBatch[월간 배치 작업<br/>매월 1일 00:20]
    MonthlyBatch --> Settlement[Settlement 생성<br/>status: PENDING]
    
    Settlement --> AddItems[SettlementItem 추가<br/>status: CONFIRMED]
    AddItems --> Complete[Settlement 완료<br/>status: COMPLETED<br/>settledAt 설정]
    
    Complete --> Query[정산 조회<br/>크리에이터]
    
    style Start fill:#e1f5ff
    style Payment fill:#c8e6c9
    style SettlementItem1 fill:#fff9c4
    style SettlementItem2 fill:#fff9c4
    style Settlement fill:#ffccbc
    style Complete fill:#c8e6c9
    style Query fill:#e1f5ff
```

---

## 결제 완료 후 원장 기록 플로우

```mermaid
sequenceDiagram
    participant PaymentSvc as PaymentService
    participant PaymentRepo as PaymentRepository
    participant SettlementItemRepo as SettlementItemRepository
    participant DB as MySQL<br/>Database
    
    Note over PaymentSvc,DB: 결제 완료 시점
    PaymentSvc->>PaymentRepo: Payment 저장<br/>(status: PAID, paidAt 설정)
    PaymentRepo->>DB: Payment 저장
    
    Note over PaymentSvc,DB: 원장 기록 (수동 또는 배치)
    PaymentSvc->>SettlementItemRepo: SettlementItem.create(payment)
    
    Note over SettlementItemRepo: SettlementItem 생성 로직<br/>- totalAmount: 결제 금액<br/>- platformFee: 결제 금액 × 10%<br/>- settlementAmount: 결제 금액 × 90%<br/>- status: RECORDED<br/>- settlement_id: null
    
    SettlementItemRepo->>DB: SettlementItem 저장<br/>(status: RECORDED, settlement_id: null)
    DB-->>SettlementItemRepo: 저장 완료
    
    Note over PaymentSvc,DB: 원장 기록 완료<br/>정산 확정 대기 상태
```

---

## 주간 원장 기록 배치 작업 플로우

```mermaid
sequenceDiagram
    participant Scheduler as SettlementScheduler
    participant UserRepo as UserRepository
    participant ItemBatchSvc as SettlementItemBatchService
    participant PaymentRepo as PaymentRepository
    participant SettlementItemRepo as SettlementItemRepository
    participant DB as MySQL<br/>Database
    
    Note over Scheduler,DB: 매주 월요일 00:10 실행
    Scheduler->>Scheduler: @Scheduled(cron = "0 10 0 * * MON")
    Scheduler->>UserRepo: findAllByRoleEnum(ROLE_CREATOR)
    UserRepo->>DB: 크리에이터 목록 조회
    DB-->>UserRepo: 크리에이터 목록
    UserRepo-->>Scheduler: List<User> creators
    
    loop 각 크리에이터별 처리
        Scheduler->>ItemBatchSvc: recordLastWeekLedger(creatorId)
        
        Note over ItemBatchSvc: 지난주 범위 계산<br/>(월요일~일요일)
        ItemBatchSvc->>PaymentRepo: findByCreator_IdAndPaidAtBetween<br/>(creatorId, start, end)
        PaymentRepo->>DB: 지난주 결제건 조회
        DB-->>PaymentRepo: List<Payment>
        PaymentRepo-->>ItemBatchSvc: List<Payment>
        
        loop 각 결제건별 처리
            ItemBatchSvc->>SettlementItemRepo: existsByPaymentId(paymentId)
            SettlementItemRepo->>DB: SettlementItem 존재 여부 확인
            DB-->>SettlementItemRepo: 존재 여부
            SettlementItemRepo-->>ItemBatchSvc: boolean
            
            alt SettlementItem이 없는 경우
                ItemBatchSvc->>ItemBatchSvc: SettlementItem.create(payment)
                Note over ItemBatchSvc: SettlementItem 생성<br/>- totalAmount: payment.amount<br/>- platformFee: amount × 0.1<br/>- settlementAmount: amount × 0.9<br/>- status: RECORDED
                ItemBatchSvc->>SettlementItemRepo: save(SettlementItem)
                SettlementItemRepo->>DB: SettlementItem 저장
                DB-->>SettlementItemRepo: 저장 완료
            else SettlementItem이 이미 있는 경우
                Note over ItemBatchSvc: 건너뛰기 (중복 방지)
            end
        end
        
        ItemBatchSvc-->>Scheduler: 생성된 SettlementItem 수
    end
    
    Note over Scheduler,DB: 주간 원장 기록 완료<br/>모든 크리에이터의 지난주 결제건이<br/>SettlementItem으로 기록됨
```

---

## 월간 정산 확정 배치 작업 플로우

```mermaid
sequenceDiagram
    participant Scheduler as SettlementScheduler
    participant UserRepo as UserRepository
    participant SettlementBatchSvc as SettlementBatchService
    participant SettlementRepo as SettlementRepository
    participant SettlementItemRepo as SettlementItemRepository
    participant DB as MySQL<br/>Database
    
    Note over Scheduler,DB: 매월 1일 00:20 실행
    Scheduler->>Scheduler: @Scheduled(cron = "0 20 0 1 * *")
    Scheduler->>UserRepo: findAllByRoleEnum(ROLE_CREATOR)
    UserRepo->>DB: 크리에이터 목록 조회
    DB-->>UserRepo: List<User> creators
    UserRepo-->>Scheduler: 크리에이터 목록
    
    loop 각 크리에이터별 처리
        Scheduler->>SettlementBatchSvc: confirmLastMonth(creatorId)
        
        Note over SettlementBatchSvc: 전월 범위 계산<br/>(1일~말일)
        SettlementBatchSvc->>SettlementItemRepo: findByCreatorIdAndStatusAndSettlementIdIsNullAndCreatedAtBetween<br/>(creatorId, RECORDED, start, end)
        SettlementItemRepo->>DB: 전월 RECORDED 상태의<br/>SettlementItem 조회<br/>(settlement_id가 null인 항목)
        DB-->>SettlementItemRepo: List<SettlementItem>
        SettlementItemRepo-->>SettlementBatchSvc: List<SettlementItem>
        
        alt SettlementItem이 없는 경우
            Note over SettlementBatchSvc: 정산할 항목 없음<br/>처리 종료
        else SettlementItem이 있는 경우
            SettlementBatchSvc->>SettlementBatchSvc: Settlement.create(creator, periodStart, periodEnd, 0L)
            Note over SettlementBatchSvc: Settlement 생성<br/>- status: PENDING<br/>- totalAmount: 0 (초기값)
            
            SettlementBatchSvc->>SettlementRepo: save(Settlement)
            SettlementRepo->>DB: Settlement 저장
            DB-->>SettlementRepo: Settlement 저장 완료
            SettlementRepo-->>SettlementBatchSvc: Settlement
            
            loop 각 SettlementItem별 처리
                SettlementBatchSvc->>Settlement: addSettlementItem(item)
                Note over Settlement: SettlementItem 추가 로직<br/>1. settlement.settlementItems.add(item)<br/>2. item.assignToSettlement(settlement)<br/>   → item.settlement = settlement<br/>   → item.status = CONFIRMED<br/>3. settlement.totalAmount += item.settlementAmount
                
                Settlement->>SettlementItemRepo: item.confirm()
                SettlementItemRepo->>DB: SettlementItem 상태 변경<br/>(RECORDED → CONFIRMED)
            end
            
            SettlementBatchSvc->>Settlement: complete()
            Note over Settlement: Settlement 완료 처리<br/>- status: PENDING → COMPLETED<br/>- settledAt: 현재 시간 설정
            
            SettlementBatchSvc->>SettlementRepo: save(Settlement)
            SettlementRepo->>DB: Settlement 업데이트
            DB-->>SettlementRepo: 업데이트 완료
        end
        
        SettlementBatchSvc-->>Scheduler: 처리 완료
    end
    
    Note over Scheduler,DB: 월간 정산 확정 완료<br/>전월의 모든 RECORDED SettlementItem이<br/>Settlement로 묶여서 확정됨
```

---

## 정산 조회 플로우

```mermaid
sequenceDiagram
    participant Creator as 크리에이터<br/>(Frontend)
    participant Frontend as Next.js<br/>페이지
    participant Backend as Spring Boot<br/>API
    participant SettlementSvc as SettlementService
    participant SettlementRepo as SettlementRepository
    participant SettlementItemRepo as SettlementItemRepository
    participant DB as MySQL<br/>Database
    
    Note over Creator,DB: 케이스 1: 정산 목록 조회
    Creator->>Frontend: 정산 내역 페이지 접속
    Frontend->>Backend: GET /api/settlements<br/>(page, size)
    Backend->>SettlementSvc: getMySettlements(creatorId, pageable)
    
    SettlementSvc->>DB: 크리에이터 권한 확인
    DB-->>SettlementSvc: User 정보
    
    SettlementSvc->>SettlementRepo: findByCreatorIdOrderByPeriodEndDesc<br/>(creatorId, pageable)
    SettlementRepo->>DB: Settlement 목록 조회<br/>(periodEnd 내림차순)
    DB-->>SettlementRepo: Page<Settlement>
    SettlementRepo-->>SettlementSvc: Page<Settlement>
    
    SettlementSvc-->>Backend: Page<SettlementResponseDto>
    Backend-->>Frontend: 200 OK
    Frontend-->>Creator: 정산 목록 표시
    
    Note over Creator,DB: 케이스 2: 정산 상세 조회
    Creator->>Frontend: 정산 상세 클릭
    Frontend->>Backend: GET /api/settlements/{settlementId}
    Backend->>SettlementSvc: getSettlementDetail(settlementId, userId, pageable)
    
    SettlementSvc->>SettlementRepo: findById(settlementId)
    SettlementRepo->>DB: Settlement 조회
    DB-->>SettlementRepo: Settlement
    SettlementRepo-->>SettlementSvc: Settlement
    
    Note over SettlementSvc: 권한 체크<br/>본인의 정산인지 확인
    
    SettlementSvc->>SettlementItemRepo: findBySettlementId(settlementId, pageable)
    SettlementItemRepo->>DB: SettlementItem 목록 조회
    DB-->>SettlementItemRepo: Page<SettlementItem>
    SettlementItemRepo-->>SettlementSvc: Page<SettlementItem>
    
    SettlementSvc-->>Backend: SettlementDetailResponse<br/>(Settlement + SettlementItem 목록)
    Backend-->>Frontend: 200 OK
    Frontend-->>Creator: 정산 상세 정보 표시
    
    Note over Creator,DB: 케이스 3: 대기 중인 정산 조회
    Creator->>Frontend: 대기 중인 정산 조회
    Frontend->>Backend: GET /api/settlements/pending<br/>(page, size)
    Backend->>SettlementSvc: getPendingSettlementItems(creatorId, pageable)
    
    SettlementSvc->>DB: 크리에이터 권한 확인
    DB-->>SettlementSvc: User 정보
    
    SettlementSvc->>SettlementItemRepo: findByCreatorIdAndSettlementIdIsNullOrderByCreatedAtDesc<br/>(creatorId, pageable)
    SettlementItemRepo->>DB: SettlementItem 조회<br/>(settlement_id가 null인 항목)
    DB-->>SettlementItemRepo: Page<SettlementItem>
    SettlementItemRepo-->>SettlementSvc: Page<SettlementItem>
    
    SettlementSvc-->>Backend: Page<SettlementItemResponse>
    Backend-->>Frontend: 200 OK
    Frontend-->>Creator: 대기 중인 정산 항목 표시
```

---

## SettlementItem 생성 로직 상세

```mermaid
flowchart TD
    Start([Payment 완료]) --> CheckPayment{Payment<br/>paidAt 확인}
    CheckPayment -->|paidAt이 null| Skip[건너뛰기]
    CheckPayment -->|paidAt이 있음| CheckExists{SettlementItem<br/>이미 존재?}
    
    CheckExists -->|존재함| Skip
    CheckExists -->|존재하지 않음| Create[SettlementItem.create<br/>payment]
    
    Create --> CalcAmounts[금액 계산]
    CalcAmounts --> TotalAmount[totalAmount<br/>= payment.amount]
    TotalAmount --> PlatformFee[platformFee<br/>= amount × 0.1<br/>10%]
    PlatformFee --> SettlementAmount[settlementAmount<br/>= amount × 0.9<br/>90%]
    
    SettlementAmount --> SetStatus[status: RECORDED]
    SetStatus --> SetSettlement[settlement_id: null]
    SetSettlement --> SetCreator[creator: payment.creator]
    SetCreator --> SetCreatedAt[createdAt: 현재 시간]
    SetCreatedAt --> Save[DB 저장]
    Save --> End([SettlementItem 생성 완료])
    
    Skip --> End
    
    style Start fill:#e1f5ff
    style End fill:#c8e6c9
    style Skip fill:#ffcdd2
    style Create fill:#fff9c4
```

---

## Settlement 확정 로직 상세

```mermaid
flowchart TD
    Start([월간 배치 시작]) --> FindItems[전월 RECORDED<br/>SettlementItem 조회<br/>settlement_id가 null]
    
    FindItems --> CheckEmpty{조회된<br/>항목이 있음?}
    CheckEmpty -->|없음| End1([처리 종료])
    CheckEmpty -->|있음| CreateSettlement[Settlement 생성<br/>status: PENDING<br/>totalAmount: 0]
    
    CreateSettlement --> SaveSettlement[DB 저장]
    SaveSettlement --> LoopStart[각 SettlementItem<br/>순회]
    
    LoopStart --> AddItem[settlement.addSettlementItem<br/>item]
    AddItem --> Step1[1. settlementItems.add<br/>item]
    Step1 --> Step2[2. item.assignToSettlement<br/>settlement]
    Step2 --> ItemStatus[item.status:<br/>RECORDED → CONFIRMED]
    ItemStatus --> Step3[3. settlement.totalAmount<br/>+= item.settlementAmount]
    Step3 --> ItemConfirm[item.confirm<br/>CONFIRMED]
    
    ItemConfirm --> CheckMore{더 많은<br/>항목?}
    CheckMore -->|있음| LoopStart
    CheckMore -->|없음| Complete[Settlement.complete]
    
    Complete --> SetStatus[status:<br/>PENDING → COMPLETED]
    SetStatus --> SetSettledAt[settledAt:<br/>현재 시간]
    SetSettledAt --> UpdateDB[DB 업데이트]
    UpdateDB --> End2([정산 확정 완료])
    
    style Start fill:#e1f5ff
    style End1 fill:#ffcdd2
    style End2 fill:#c8e6c9
    style CreateSettlement fill:#fff9c4
    style Complete fill:#ffccbc
```

---

## 정산 상태 전이도

```mermaid
stateDiagram-v2
    [*] --> PaymentCompleted: 결제 완료<br/>Payment.status = PAID
    
    PaymentCompleted --> SettlementItemRecorded: 원장 기록<br/>(수동 또는 배치)
    
    SettlementItemRecorded: SettlementItem<br/>status: RECORDED<br/>settlement_id: null
    
    SettlementItemRecorded --> SettlementItemConfirmed: 월간 배치<br/>정산 확정
    
    SettlementItemConfirmed: SettlementItem<br/>status: CONFIRMED<br/>settlement_id: 설정됨
    
    SettlementItemConfirmed --> SettlementPending: Settlement 생성
    
    SettlementPending: Settlement<br/>status: PENDING<br/>totalAmount: 0
    
    SettlementPending --> SettlementCompleted: SettlementItem 추가<br/>및 완료 처리
    
    SettlementCompleted: Settlement<br/>status: COMPLETED<br/>settledAt: 설정됨
    
    SettlementCompleted --> [*]: 정산 완료
    
    note right of SettlementItemRecorded
        원장 기록 단계
        - 결제 금액의 90%가 정산 금액
        - 플랫폼 수수료 10%
        - 아직 Settlement에 포함되지 않음
    end note
    
    note right of SettlementPending
        정산 생성 단계
        - 전월의 RECORDED 항목들을 묶음
        - totalAmount는 0에서 시작
    end note
    
    note right of SettlementCompleted
        정산 확정 단계
        - 모든 SettlementItem이 추가됨
        - totalAmount가 최종 계산됨
        - settledAt이 설정됨
    end note
```

---

## 데이터 모델 관계도

```mermaid
erDiagram
    USER ||--o{ SETTLEMENT : "크리에이터"
    USER ||--o{ SETTLEMENT_ITEM : "크리에이터"
    USER ||--o{ PAYMENT : "결제"
    
    PAYMENT ||--|| SETTLEMENT_ITEM : "1:1"
    SETTLEMENT ||--o{ SETTLEMENT_ITEM : "1:N"
    
    SETTLEMENT {
        bigint id PK
        bigint creator_id FK
        date period_start "정산 기간 시작"
        date period_end "정산 기간 종료"
        bigint total_amount "총 정산 금액"
        enum status "PENDING, COMPLETED, FAILED"
        datetime settled_at "정산 확정 시간"
    }
    
    SETTLEMENT_ITEM {
        bigint id PK
        bigint payment_id FK UK "결제 ID (1:1)"
        bigint settlement_id FK "정산 ID (null 가능)"
        bigint creator_id FK
        bigint total_amount "결제 금액"
        bigint platform_fee "플랫폼 수수료 (10%)"
        bigint settlement_amount "정산 금액 (90%)"
        enum status "RECORDED, CONFIRMED"
        datetime created_at "원장 기록 시간"
    }
    
    PAYMENT {
        bigint id PK
        string orderId
        string paymentKey
        bigint user_id FK
        bigint creator_id FK
        bigint amount "결제 금액"
        enum status "PAID, CANCELED"
        datetime paidAt "결제 완료 시간"
    }
    
    note right of SETTLEMENT
        월간 정산 단위
        - 전월 1일~말일 기준
        - 여러 SettlementItem을 묶음
        - totalAmount는 모든 settlementAmount의 합
    end note
    
    note right of SETTLEMENT_ITEM
        개별 정산 항목
        - Payment와 1:1 관계
        - settlement_id가 null이면 대기 중
        - RECORDED: 원장 기록됨
        - CONFIRMED: 정산 확정됨
    end note
    
    note right of PAYMENT
        결제 정보
        - 결제 완료 시 SettlementItem 생성
        - paidAt 기준으로 원장 기록
    end note
```

---

## 정산 금액 계산 로직

```mermaid
graph LR
    Payment[결제 금액<br/>10,000원] --> TotalAmount[totalAmount<br/>10,000원]
    
    TotalAmount --> PlatformFee[platformFee<br/>10,000 × 0.1<br/>= 1,000원<br/>10%]
    TotalAmount --> SettlementAmount[settlementAmount<br/>10,000 × 0.9<br/>= 9,000원<br/>90%]
    
    PlatformFee --> Platform[플랫폼 수익]
    SettlementAmount --> Creator[크리에이터 정산 금액]
    
    style Payment fill:#e1f5ff
    style TotalAmount fill:#fff9c4
    style PlatformFee fill:#ffcdd2
    style SettlementAmount fill:#c8e6c9
    style Platform fill:#ffcdd2
    style Creator fill:#c8e6c9
```

---

## 배치 작업 스케줄

```mermaid
gantt
    title 정산 배치 작업 스케줄
    dateFormat YYYY-MM-DD
    section 주간 원장 기록
    매주 월요일 00:10 실행    :2024-01-01, 7d
    section 월간 정산 확정
    매월 1일 00:20 실행       :2024-01-01, 1d
```

### 배치 작업 상세

#### 1. 주간 원장 기록 (Weekly Ledger Recording)
- **스케줄**: 매주 월요일 00:10
- **크론 표현식**: `0 10 0 * * MON`
- **작업 내용**:
  - 모든 크리에이터에 대해 지난주(월요일~일요일) 결제건 조회
  - SettlementItem이 없는 Payment에 대해 SettlementItem 생성
  - status: RECORDED, settlement_id: null

#### 2. 월간 정산 확정 (Monthly Settlement Confirmation)
- **스케줄**: 매월 1일 00:20
- **크론 표현식**: `0 20 0 1 * *`
- **작업 내용**:
  - 모든 크리에이터에 대해 전월(1일~말일) SettlementItem 조회
  - status가 RECORDED이고 settlement_id가 null인 항목만 대상
  - Settlement 생성 및 SettlementItem 연결
  - Settlement.status: COMPLETED, SettlementItem.status: CONFIRMED

---

## 주요 API 엔드포인트

### 정산 조회
- `GET /api/settlements` - 정산 목록 조회 (페이지네이션)
- `GET /api/settlements/{settlementId}` - 정산 상세 조회
- `GET /api/settlements/pending` - 대기 중인 정산 항목 조회

### 요청/응답 예시

#### 정산 목록 조회
```json
GET /api/settlements?page=0&size=10

Response:
{
  "content": [
    {
      "id": 1,
      "periodStart": "2024-01-01",
      "periodEnd": "2024-01-31",
      "totalAmount": 900000,
      "status": "COMPLETED",
      "settledAt": "2024-02-01T00:20:00",
      "creatorNickname": "creator1"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

#### 정산 상세 조회
```json
GET /api/settlements/1?page=0&size=10

Response:
{
  "id": 1,
  "periodStart": "2024-01-01",
  "periodEnd": "2024-01-31",
  "totalAmount": 900000,
  "status": "COMPLETED",
  "settledAt": "2024-02-01T00:20:00",
  "items": {
    "content": [
      {
        "id": 1,
        "paymentId": 10,
        "totalAmount": 10000,
        "platformFee": 1000,
        "settlementAmount": 9000,
        "status": "CONFIRMED",
        "createdAt": "2024-01-15T10:30:00"
      }
    ]
  }
}
```

#### 대기 중인 정산 조회
```json
GET /api/settlements/pending?page=0&size=10

Response:
{
  "content": [
    {
      "id": 20,
      "paymentId": 25,
      "totalAmount": 10000,
      "platformFee": 1000,
      "settlementAmount": 9000,
      "status": "RECORDED",
      "createdAt": "2024-02-05T14:20:00"
    }
  ]
}
```

---

## 주요 비즈니스 로직

### 1. SettlementItem 생성 규칙
- Payment가 완료(PAID)된 경우에만 생성
- 한 Payment당 하나의 SettlementItem만 존재 (중복 방지)
- 플랫폼 수수료: 결제 금액의 10%
- 정산 금액: 결제 금액의 90%
- 초기 상태: RECORDED, settlement_id: null

### 2. 주간 원장 기록 규칙
- 지난주(월요일~일요일) 범위의 결제건만 대상
- 이미 SettlementItem이 존재하는 Payment는 건너뛰기
- 배치 작업은 모든 크리에이터에 대해 순차 실행

### 3. 월간 정산 확정 규칙
- 전월(1일~말일) 범위의 SettlementItem만 대상
- status가 RECORDED이고 settlement_id가 null인 항목만 포함
- Settlement의 totalAmount는 모든 settlementAmount의 합
- SettlementItem 추가 시 자동으로 status가 CONFIRMED로 변경

### 4. 정산 조회 권한
- 크리에이터(ROLE_CREATOR)만 조회 가능
- 본인의 정산만 조회 가능 (권한 체크)
- 대기 중인 정산은 settlement_id가 null인 항목만 조회

---

## 에러 처리

### 정산 관련 에러
- `SETTLEMENT_NOT_FOUND`: 정산을 찾을 수 없음
- `SETTLEMENT_FORBIDDEN`: 본인의 정산이 아님 (권한 없음)
- `CREATOR_FORBIDDEN`: 크리에이터 권한이 없음

### 배치 작업 에러 처리
- 각 크리에이터별로 독립적으로 처리
- 한 크리에이터의 처리 실패가 다른 크리에이터에 영향 없음
- 에러 발생 시 로그 기록 후 다음 크리에이터 처리 계속

---

## 정산 플로우 타임라인

```mermaid
timeline
    title 정산 플로우 타임라인 예시 (2024년 1월)
    
    section 1월
        1월 1일~31일 : 결제 발생
                        Payment 생성
                        SettlementItem 생성 (RECORDED)
    
    section 2월
        2월 1일 00:20 : 월간 배치 실행
                        전월 SettlementItem 조회
                        Settlement 생성 (PENDING)
                        SettlementItem 연결 (CONFIRMED)
                        Settlement 완료 (COMPLETED)
    
    section 이후
        크리에이터 : 정산 내역 조회 가능
                    정산 상세 확인 가능
```

---

## 정산 금액 계산 예시

### 시나리오: 1월에 10건의 결제 발생

| 결제 금액 | 플랫폼 수수료 (10%) | 정산 금액 (90%) |
|----------|-------------------|----------------|
| 10,000원 | 1,000원 | 9,000원 |
| 20,000원 | 2,000원 | 18,000원 |
| 15,000원 | 1,500원 | 13,500원 |
| ... | ... | ... |
| **총 100,000원** | **10,000원** | **90,000원** |

### 2월 1일 정산 확정 시
- **Settlement.totalAmount**: 90,000원
- **SettlementItem 개수**: 10개
- **모든 SettlementItem.status**: CONFIRMED
- **Settlement.status**: COMPLETED


