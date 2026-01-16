import apiClient from '@/src/lib/axios';
import { ReportCreateRequest, ReportResponse } from '@/src/types/report';

export const reportApi = {
  // 신고하기
  createReport: async (data: ReportCreateRequest): Promise<ReportResponse> => {
    const response = await apiClient.post<ReportResponse>('/api/report', data);
    return response.data;
  },
};
