'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';

interface AuthState {
  isAuthenticated: boolean;
  isCreator: boolean;
  isLoading: boolean;
}

export function useAuth() {
  const router = useRouter();
  const [authState, setAuthState] = useState<AuthState>({
    isAuthenticated: false,
    isCreator: false,
    isLoading: true,
  });

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      // TODO: 백엔드에서 인증 상태 확인 API 필요 (예: GET /api/users/me)
      // 현재는 쿠키 존재 여부로만 판단
      // 실제 구현 시:
      // const response = await userApi.getMe();
      // setAuthState({ isAuthenticated: true, isCreator: response.role === 'ROLE_CREATOR', isLoading: false });
      
      setAuthState({ isAuthenticated: false, isCreator: false, isLoading: false });
    } catch (error) {
      setAuthState({ isAuthenticated: false, isCreator: false, isLoading: false });
    }
  };

  return { ...authState, checkAuth };
}

