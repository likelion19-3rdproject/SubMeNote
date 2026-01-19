import apiClient from '@/src/lib/axios';

export const profileImageApi = {
  // 프로필 이미지 업로드/교체
  uploadProfileImage: async (file: File): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);

    await apiClient.post('/api/profile-images/me', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 프로필 이미지 URL 가져오기
  getProfileImageUrl: (userId: number): string => {
    const envUrl =
      process.env.NEXT_PUBLIC_API_URL ||
      // 과거/오타 키 호환
      process.env.NEXT_PUBLIC_API_BASE_URL ||
      (process.env as any).NEXT_PUBLIC_API_Base_URL;

    if (envUrl) {
      return `${envUrl}/api/profile-images/users/${userId}`;
    }

    // 브라우저에서는 현재 접속 호스트 기준으로 8080 사용
    if (typeof window !== 'undefined') {
      return `${window.location.protocol}//${window.location.hostname}:8080/api/profile-images/users/${userId}`;
    }

    // 서버/컨테이너 환경
    return `http://back:8080/api/profile-images/users/${userId}`;
  },
};
