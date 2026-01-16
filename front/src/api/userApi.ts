import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { PostResponseDto } from '@/src/types/post';
import { CommentResponseDto } from '@/src/types/comment';
import { AccountRequest, UserResponseDto } from '@/src/types/user';

export const userApi = {
  getMe: async (): Promise<UserResponseDto> => {
    const response = await apiClient.get<UserResponseDto>('/api/users/me');
    return response.data;
  },
  getMyPosts: async (): Promise<Page<PostResponseDto>> => {
    const response = await apiClient.get<Page<PostResponseDto>>('/api/users/me/posts');
    return response.data;
  },

  getMyComments: async (): Promise<Page<CommentResponseDto>> => {
    const response = await apiClient.get<Page<CommentResponseDto>>('/api/users/me/comments');
    return response.data;
  },

  createAccount: async (data: AccountRequest): Promise<void> => {
    await apiClient.post('/api/users/me/account', data);
  },

  updateAccount: async (data: AccountRequest): Promise<void> => {
    await apiClient.patch('/api/users/me/account', data);
  },

  deleteUser: async (): Promise<void> => {
    await apiClient.delete('/api/users/me');
  },
};

