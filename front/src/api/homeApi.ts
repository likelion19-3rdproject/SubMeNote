import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { CreatorResponseDto } from '@/src/types/home';

export const homeApi = {
  getCreators: async (page: number = 0, size: number = 10): Promise<Page<CreatorResponseDto>> => {
    const response = await apiClient.get<Page<CreatorResponseDto>>('/api/home', {
      params: { page, size },
    });
    return response.data;
  },

  searchCreators: async (keyword: string, page: number = 0, size: number = 10): Promise<Page<CreatorResponseDto>> => {
    const response = await apiClient.get<Page<CreatorResponseDto>>('/api/home/search', {
      params: { keyword, page, size },
    });
    return response.data;
  },
};

