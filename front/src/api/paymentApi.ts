import apiClient from '@/src/lib/axios';
import { PaymentConfirmRequest, PaymentResponse } from '@/src/types/payment';

export const paymentApi = {
  confirmPayment: async (data: PaymentConfirmRequest): Promise<PaymentResponse> => {
    const response = await apiClient.post<PaymentResponse>('/api/payments/confirm', data);
    return response.data;
  },

  // [이동됨] 실패 처리는 결제 도메인의 역할
  failPayment: async (data: { orderId: string; code: string; message: string }): Promise<void> => {
    // URL 변경: /api/orders/fail -> /api/payments/fail
    await apiClient.post('/api/payments/fail', data);
  },
};