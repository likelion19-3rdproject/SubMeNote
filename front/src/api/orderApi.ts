import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { OrderCreateRequest, OrderCreateResponseDto, OrderResponseDto } from '@/src/types/order';

export const orderApi = {
  createOrder: async (data: OrderCreateRequest): Promise<OrderCreateResponseDto> => {
    const response = await apiClient.post<OrderCreateResponseDto>('/api/orders', data);
    return response.data;
  },

  getOrders: async (page: number = 0, size: number = 10): Promise<Page<OrderResponseDto>> => {
    const response = await apiClient.get<Page<OrderResponseDto>>('/api/orders', {
      params: { page, size },
    });
    return response.data;
  },

  getOrder: async (orderId: number): Promise<OrderResponseDto> => {
    const response = await apiClient.get<OrderResponseDto>(`/api/orders/${orderId}`);
    return response.data;
  },

  
};