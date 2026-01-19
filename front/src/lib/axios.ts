import axios, { AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';
import { authApi } from '@/src/api/authApi';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 리프레시 토큰 갱신 중인지 추적 (동시 요청 처리)
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
  config: AxiosRequestConfig;
}> = [];

// 큐에 대기 중인 요청들을 처리
const processQueue = (error: AxiosError | null) => {
  failedQueue.forEach(({ resolve, reject, config }) => {
    if (error) {
      reject(error);
    } else {
      // 토큰이 갱신되었으므로 원래 요청 재시도
      resolve(apiClient(config));
    }
  });
  failedQueue = [];
};

// Request Interceptor
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor - 401 처리 및 리프레시 토큰 자동 갱신
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    // 401 에러가 아니거나 재시도한 요청이면 그대로 reject
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    // 클라이언트 사이드에서만 처리
    if (typeof window === 'undefined') {
      return Promise.reject(error);
    }

    // /api/auth/refresh 요청 자체가 401이면 무한 루프 방지를 위해 로그인 페이지로 리다이렉트
    if (originalRequest.url?.includes('/api/auth/refresh')) {
      const currentPath = window.location.pathname;
      const isPublicPath = currentPath === '/' || 
                          currentPath === '/login' || 
                          currentPath === '/signup' ||
                          currentPath.startsWith('/creators/') ||
                          currentPath.startsWith('/posts/');
      
      // 공개 페이지가 아니면 로그인 페이지로 리다이렉트
      if (!isPublicPath) {
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }

    // 이미 리프레시 중이면 큐에 추가하고 대기
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject, config: originalRequest });
      });
    }

    // 리프레시 시작 (공개 페이지에서도 리프레시 토큰 갱신 시도)
    originalRequest._retry = true;
    isRefreshing = true;

    try {
      // 리프레시 토큰으로 새로운 액세스 토큰 발급
      await authApi.refresh();

      // 큐에 대기 중인 모든 요청 처리
      processQueue(null);

      // 원래 요청 재시도 (새로운 토큰으로)
      return apiClient(originalRequest);
    } catch (refreshError) {
      // 리프레시 실패 시 큐의 모든 요청을 실패 처리
      processQueue(refreshError as AxiosError);

      // 공개 페이지 체크 (리프레시 실패 시에만 리다이렉트 여부 결정)
      const currentPath = window.location.pathname;
      
      // 공개 페이지 목록 (인증 없이 접근 가능한 페이지)
      const publicPaths = [
        '/',                      // 메인페이지
        '/login',                 // 로그인 페이지
        '/signup',                // 회원가입 페이지
      ];
      
      // 공개 경로 패턴 (동적 라우트 포함)
      const isPublicPath = publicPaths.includes(currentPath) || 
                         currentPath.startsWith('/creators/') ||  // 크리에이터 상세 페이지
                         currentPath.startsWith('/posts/');       // 게시글 상세 페이지

      // 공개 페이지가 아닐 때만 로그인 페이지로 리다이렉트
      if (!isPublicPath) {
        window.location.href = '/login';
      }
      
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default apiClient;

