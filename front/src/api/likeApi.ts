import apiClient from '@/src/lib/axios';
import { LikeToggleResult } from '@/src/types/like';

export const likeApi = {
  // 게시글 좋아요 토글
  togglePostLike: async (postId: number): Promise<LikeToggleResult> => {
    const response = await apiClient.post<LikeToggleResult>(
      `/api/posts/${postId}/likes`
    );
    return response.data;
  },

  // 댓글 좋아요 토글
  toggleCommentLike: async (commentId: number): Promise<LikeToggleResult> => {
    const response = await apiClient.post<LikeToggleResult>(
      `/api/comments/${commentId}/likes`
    );
    return response.data;
  },
};
