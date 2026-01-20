import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { PostResponseDto } from '@/src/types/post';
import { CommentResponseDto } from '@/src/types/comment';
import { AccountRequest, AccountResponse, UserResponseDto } from '@/src/types/user';

export const userApi = {
  getMe: async (): Promise<UserResponseDto> => {
    const response = await apiClient.get<UserResponseDto>('/api/users/me');
    return response.data;
  },
  getMyPosts: async (): Promise<Page<PostResponseDto>> => {
    const response = await apiClient.get<Page<PostResponseDto>>('/api/creator/me/posts');
    return response.data;
  },

  getMyComments: async (): Promise<Page<CommentResponseDto>> => {
    const response = await apiClient.get<Page<CommentResponseDto>>('/api/users/me/comments');
    return response.data;
  },

  getAccount: async (): Promise<AccountResponse> => {
    const response = await apiClient.get<AccountResponse>('/api/creator/me/account');
    return response.data;
  },

  createAccount: async (data: AccountRequest): Promise<void> => {
    await apiClient.post('/api/creator/me/account', data);
  },

  updateAccount: async (data: AccountRequest): Promise<void> => {
    await apiClient.patch('/api/creator/me/account', data);
  },

  deleteUser: async (): Promise<void> => {
    await apiClient.delete('/api/users/me');
  },
};

