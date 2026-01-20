import apiClient from '@/src/lib/axios';
import { LoginRequest, SignupRequest, LoginResponse, CheckDuplicationResponse } from '@/src/types/auth';

export const authApi = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>('/api/auth/login', data);
    return response.data;
  },

  signup: async (data: SignupRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>('/api/auth/signup', data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/api/auth/logout');
  },

  refresh: async (): Promise<void> => {
    // refresh 요청은 인터셉터를 거치지 않도록 _skipRefresh 플래그 설정
    await apiClient.post('/api/auth/refresh', {}, {
      _skipRefresh: true,
    } as any);
  },

  checkDuplication: async (nickname: string): Promise<CheckDuplicationResponse> => {
    const response = await apiClient.post<boolean>(
        '/api/auth/check-duplication', 
        nickname,
        {
            headers: {
                'Content-Type': 'text/plain'
            }
        }
    );
    return { available: response.data };
  },
};

