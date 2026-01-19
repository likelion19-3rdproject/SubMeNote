import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import {
  NotificationResponseDto,
  NotificationReadRequest,
  NotificationReadResponse,
} from '@/src/types/notification';

export const notificationApi = {
  // 알림 목록 조회
  getNotifications: async (page: number = 0, size: number = 20): Promise<Page<NotificationResponseDto>> => {
    const response = await apiClient.get<Page<NotificationResponseDto>>('/api/notifications', {
      params: { page, size },
    });
    return response.data;
  },

  // 알림 상세 조회
  getNotification: async (notificationId: number): Promise<NotificationResponseDto> => {
    const response = await apiClient.get<NotificationResponseDto>(`/api/notifications/${notificationId}`);
    return response.data;
  },

  // 알림 읽음 처리
  readNotifications: async (notificationIds: number[]): Promise<NotificationReadResponse> => {
    const request: NotificationReadRequest = { notificationIds };
    const response = await apiClient.patch<NotificationReadResponse>('/api/notifications/read', request);
    return response.data;
  },

  // 알림 삭제
  deleteNotification: async (notificationId: number): Promise<void> => {
    await apiClient.delete(`/api/notifications/${notificationId}`);
  },
};
