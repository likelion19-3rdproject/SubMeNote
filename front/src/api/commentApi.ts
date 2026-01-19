import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { CommentResponseDto, CommentCreateRequest, CommentUpdateRequest } from '@/src/types/comment';

export const commentApi = {
  getComments: async (postId: number): Promise<Page<CommentResponseDto>> => {
    const response = await apiClient.get<Page<CommentResponseDto>>(`/api/posts/${postId}/comments`);
    return response.data;
  },

  createComment: async (postId: number, data: CommentCreateRequest, parentId?: number | null): Promise<CommentResponseDto> => {
    const requestData: CommentCreateRequest = parentId !== undefined && parentId !== null 
      ? { ...data, parentId } 
      : data;
    const response = await apiClient.post<CommentResponseDto>(`/api/posts/${postId}/comments`, requestData);
    return response.data;
  },

  updateComment: async (commentId: number, data: CommentUpdateRequest): Promise<CommentResponseDto> => {
    const response = await apiClient.patch<CommentResponseDto>(`/api/comments/${commentId}`, data);
    return response.data;
  },

  deleteComment: async (commentId: number): Promise<void> => {
    await apiClient.delete(`/api/comments/${commentId}`);
  },
};

