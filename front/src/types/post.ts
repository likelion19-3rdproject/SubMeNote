export interface PostResponseDto {
  id: number;
  userId: number; // 백엔드는 userId를 사용
  nickname: string; // 백엔드는 nickname을 사용 (creatorNickname 아님)
  title: string;
  content: string;
  visibility: 'PUBLIC' | 'SUBSCRIBERS_ONLY';
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  likedByMe: boolean;
}

export interface PostCreateRequest {
  title: string;
  content: string;
  visibility: 'PUBLIC' | 'SUBSCRIBERS_ONLY';
}

export interface PostUpdateRequest {
  title?: string;
  content?: string;
  visibility?: 'PUBLIC' | 'SUBSCRIBERS_ONLY';
}

