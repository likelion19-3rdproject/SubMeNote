import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { CreatorApplicationResponse, ApplicationProcessRequest } from '@/src/types/application';
import {
  HiddenPostResponse,
  HiddenCommentResponse,
  ReportRestoreRequest,
  ReportDeleteRequest,
} from '@/src/types/report';

export const adminApi = {
  // 크리에이터 신청 목록 조회
  getCreatorApplications: async (page: number = 0, size: number = 10): Promise<Page<CreatorApplicationResponse>> => {
    const response = await apiClient.get<Page<CreatorApplicationResponse>>('/api/admin/creator-applications', {
      params: { page, size },
    });
    return response.data;
  },

  // 크리에이터 신청 승인/거절
  processApplication: async (applicationId: number, data: ApplicationProcessRequest): Promise<void> => {
    await apiClient.patch(`/api/admin/creator-applications/${applicationId}`, data);
  },

  // 숨김 게시글 목록
  getHiddenPosts: async (page: number = 0, size: number = 20): Promise<Page<HiddenPostResponse>> => {
    const response = await apiClient.get<Page<HiddenPostResponse>>('/api/report/admin/posts', {
      params: { page, size },
    });
    return response.data;
  },

  // 숨김 댓글 목록
  getHiddenComments: async (page: number = 0, size: number = 20): Promise<Page<HiddenCommentResponse>> => {
    const response = await apiClient.get<Page<HiddenCommentResponse>>('/api/report/admin/comments', {
      params: { page, size },
    });
    return response.data;
  },

  // 게시글/댓글 복구
  restoreContent: async (data: ReportRestoreRequest): Promise<void> => {
    await apiClient.patch('/api/report/admin/restore', data);
  },

  // 게시글/댓글 삭제
  deleteContent: async (data: ReportDeleteRequest): Promise<void> => {
    await apiClient.delete('/api/report/admin/delete', { data });
  },
};
