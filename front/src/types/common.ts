// 공통 Page 타입
export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// 공통 응답 타입
export interface ApiResponse<T> {
  data: T;
}

