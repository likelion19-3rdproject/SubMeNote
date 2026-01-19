import { Page } from './common';

// 정산 상태 enum
export type SettlementStatus = 'PENDING' | 'COMPLETED' | 'FAILED';

// 정산 원장 상태 enum
export type SettlementItemStatus = 'RECORDED' | 'CONFIRMED';

// 정산 목록 조회 응답 (SettlementResponseDto)
export interface SettlementResponseDto {
  id: number;
  creatorId: number;
  creatorNickname: string;
  periodStart: string; // LocalDate -> string (YYYY-MM-DD)
  periodEnd: string; // LocalDate -> string (YYYY-MM-DD)
  totalAmount: number;
  status: SettlementStatus;
  settledAt: string | null; // LocalDateTime -> string (ISO 8601), nullable
}

// 정산 상세 조회 응답 (SettlementDetailResponse)
export interface SettlementDetailResponse {
  settlementId: number;
  periodStart: string; // LocalDate -> string (YYYY-MM-DD)
  periodEnd: string; // LocalDate -> string (YYYY-MM-DD)
  totalAmount: number;
  status: SettlementStatus;
  settledAt: string | null; // LocalDateTime -> string (ISO 8601), nullable
  items: Page<SettlementItemResponse>;
}

// 정산 원장 항목 (SettlementItemResponse)
export interface SettlementItemResponse {
  id: number;
  paymentId: number;
  totalAmount: number; // 결제 금액
  platformFee: number; // 플랫폼 수수료 (10%)
  settlementAmount: number; // 정산 금액 (90%)
  status: SettlementItemStatus;
  createdAt: string; // LocalDateTime -> string (ISO 8601)
}

