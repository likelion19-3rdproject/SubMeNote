export interface OrderCreateRequest {
  creatorId: number;
  orderName: string;
  amount: number;
}

export interface OrderCreateResponseDto {
  orderId: string;
  orderName: string;
  amount: number;
}

export interface OrderResponseDto {
  id: number;
  orderId: string;
  orderName: string;
  amount: number;
  status: string;
  createdAt: string;
  creatorId?: number;
  creatorNickname?: string;
}
