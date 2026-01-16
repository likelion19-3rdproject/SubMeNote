import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { SubscribeResponseDto, SubscribedCreatorResponseDto, SubscribeUpdateRequest } from '@/src/types/subscribe';

export const subscribeApi = {
  subscribe: async (creatorId: number): Promise<SubscribeResponseDto> => {
    const response = await apiClient.post<SubscribeResponseDto>(`/api/subscribes/${creatorId}`);
    return response.data;
  },

  updateSubscribe: async (subscribeId: number, data: SubscribeUpdateRequest): Promise<SubscribeResponseDto> => {
    const response = await apiClient.patch<SubscribeResponseDto>(`/api/subscribes/${subscribeId}`, data);
    return response.data;
  },

  deleteSubscribe: async (subscribeId: number): Promise<void> => {
    await apiClient.delete(`/api/subscribes/${subscribeId}`);
  },

  getMyCreators: async (page: number = 0, size: number = 10): Promise<Page<SubscribedCreatorResponseDto>> => {
    const response = await apiClient.get<Page<SubscribedCreatorResponseDto>>('/api/subscribes/my-creator', {
      params: { page, size },
    });
    return response.data;
  },
};

