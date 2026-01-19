import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

function resolveApiBaseUrl(): string {
  // 1) 명시된 환경변수 우선 (프로덕션/로컬 모두)
  const envUrl =
    process.env.NEXT_PUBLIC_API_URL ||
    // 과거/오타 키 호환
    process.env.NEXT_PUBLIC_API_BASE_URL;
  if (envUrl) return envUrl;

  // 2) 브라우저 환경이면, 현재 접속한 호스트를 기준으로 8080으로 치환
  if (typeof window !== 'undefined') {
    const { protocol, hostname } = window.location;
    return `${protocol}//${hostname}:8080`;
  }

  // 3) SSR/서버 환경(컨테이너 내부)에서는 docker compose 서비스명으로 접근
  return 'http://back:8080';
}

const apiClient = axios.create({
  baseURL: resolveApiBaseUrl(),
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor - 401 처리
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // 클라이언트 사이드에서만 리다이렉트
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;

