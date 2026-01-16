export interface PaymentConfirmRequest {
  paymentKey: string;
  orderId: string;
  amount: number;
}

export interface PaymentResponse {
  paymentKey: string;
  orderId: string;
  amount: number;
  status: string;
  // 추가 필드는 백엔드 응답에 따라 확장
}

