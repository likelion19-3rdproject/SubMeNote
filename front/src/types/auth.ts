export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  nickname: string;
  password: string;
  role: 'ROLE_USER' | 'ROLE_CREATOR';
}

export interface LoginResponse {
  refreshToken: string;
}

export interface CheckDuplicationResponse {
  available: boolean;
}

