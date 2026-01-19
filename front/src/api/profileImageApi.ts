import apiClient from '@/src/lib/axios';

export const profileImageApi = {
  // 프로필 이미지 업로드/교체
  uploadProfileImage: async (file: File): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);

    await apiClient.post('/api/creator/me/profile-images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 프로필 이미지 URL 가져오기
  getProfileImageUrl: (userId: number): string => {
    return `${process.env.NEXT_PUBLIC_API_Base_URL || 'http://localhost:8080'}/api/profile-images/users/${userId}`;
  },
};
