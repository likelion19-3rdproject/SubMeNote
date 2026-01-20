import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { SettlementResponseDto, SettlementDetailResponse, SettlementItemResponse } from '@/src/types/settlement';

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

  // 대기 중인 정산 조회 (settlement_id가 null인 SettlementItem)
  getPendingSettlementItems: async (page: number = 0, size: number = 10): Promise<Page<SettlementItemResponse>> => {
    const response = await apiClient.get<Page<SettlementItemResponse>>('/api/settlements/pending', {
      params: { page, size },
    });
    return response.data;
  },

  // 즉시 정산 처리
  settleImmediately: async (): Promise<SettlementResponseDto> => {
    const response = await apiClient.post<SettlementResponseDto>('/api/settlements/immediate');
    return response.data;
  },
};

