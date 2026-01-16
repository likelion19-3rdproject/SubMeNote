import apiClient from '@/src/lib/axios';
import { PaymentConfirmRequest, PaymentResponse } from '@/src/types/payment';

export const paymentApi = {
  confirmPayment: async (data: PaymentConfirmRequest): Promise<PaymentResponse> => {
    const response = await apiClient.post<PaymentResponse>('/api/payments/confirm', data);
    return response.data;
  },
};

