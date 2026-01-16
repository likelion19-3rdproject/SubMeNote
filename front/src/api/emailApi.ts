import apiClient from '@/src/lib/axios';
import { SendEmailRequest, VerifyEmailRequest, VerifyEmailResponse } from '@/src/types/email';

export const emailApi = {
  send: async (data: SendEmailRequest): Promise<void> => {
    await apiClient.post('/api/email/send', data);
  },

  resend: async (data: SendEmailRequest): Promise<void> => {
    await apiClient.post('/api/email/resend', data);
  },

  verify: async (data: VerifyEmailRequest): Promise<VerifyEmailResponse> => {
    const response = await apiClient.post<boolean>('/api/email/verify', data);
    return { verified: response.data };
  },
};

