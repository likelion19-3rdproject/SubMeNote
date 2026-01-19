import apiClient from '@/src/lib/axios';
import { CreatorApplicationResponse } from '@/src/types/application';

export const applicationApi = {
  // 크리에이터 신청
  applyForCreator: async (): Promise<void> => {
    await apiClient.post('/api/users/me/creator-application');
  },

  // 내 크리에이터 신청 내역 조회
  getMyApplication: async (): Promise<CreatorApplicationResponse> => {
    const response = await apiClient.get<CreatorApplicationResponse>('/api/users/me/creator-application');
    return response.data;
  },
};
