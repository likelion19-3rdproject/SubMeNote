import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { SettlementResponseDto, SettlementDetailResponse } from '@/src/types/settlement';

export const settlementApi = {
  getSettlements: async (page: number = 0, size: number = 10): Promise<Page<SettlementResponseDto>> => {
    const response = await apiClient.get<Page<SettlementResponseDto>>('/api/settlements', {
      params: { page, size },
    });
    return response.data;
  },

  getSettlement: async (settlementId: number): Promise<SettlementDetailResponse> => {
    const response = await apiClient.get<SettlementDetailResponse>(`/api/settlements/${settlementId}`);
    return response.data;
  },
};

