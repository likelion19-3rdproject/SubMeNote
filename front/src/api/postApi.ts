import apiClient from '@/src/lib/axios';
import { Page } from '@/src/types/common';
import { PostResponseDto, PostCreateRequest, PostUpdateRequest } from '@/src/types/post';

export const postApi = {
  getPosts: async (): Promise<Page<PostResponseDto>> => {
    const response = await apiClient.get<Page<PostResponseDto>>('/api/posts');
    return response.data;
  },

  searchSubscribedPosts: async (keyword: string, page: number = 0, size: number = 10): Promise<Page<PostResponseDto>> => {
    const response = await apiClient.get<Page<PostResponseDto>>('/api/posts/search', {
      params: { keyword, page, size },
    });
    return response.data;
  },

  getPostsByCreator: async (creatorId: number): Promise<Page<PostResponseDto>> => {
    const response = await apiClient.get<Page<PostResponseDto>>(`/api/posts/creators/${creatorId}`);
    return response.data;
  },

  getPost: async (postId: number): Promise<PostResponseDto> => {
    const response = await apiClient.get<PostResponseDto>(`/api/posts/${postId}`);
    return response.data;
  },

  createPost: async (data: PostCreateRequest): Promise<PostResponseDto> => {
    const response = await apiClient.post<PostResponseDto>('/api/posts', data);
    return response.data;
  },

  updatePost: async (postId: number, data: PostUpdateRequest): Promise<PostResponseDto> => {
    const response = await apiClient.patch<PostResponseDto>(`/api/posts/${postId}`, data);
    return response.data;
  },

  deletePost: async (postId: number): Promise<void> => {
    await apiClient.delete(`/api/posts/${postId}`);
  },
};

