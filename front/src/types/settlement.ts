export interface SettlementResponseDto {
  id: number;
  amount: number;
  status: string;
  createdAt: string;
  // 추가 필드는 백엔드 응답에 따라 확장
}

export interface SettlementDetailResponse {
  id: number;
  amount: number;
  status: string;
  createdAt: string;
  items: SettlementItemDto[];
}

export interface SettlementItemDto {
  id: number;
  amount: number;
  orderId: number;
  createdAt: string;
  // 추가 필드는 백엔드 응답에 따라 확장
}

