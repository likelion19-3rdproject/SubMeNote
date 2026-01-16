export interface CommentResponseDto {
  id: number;
  content: string;
  postId: number;
  postTitle: string;
  userId: number;
  nickname: string;
  createdAt: string;
  updatedAt: string;
}

export interface CommentCreateRequest {
  content: string;
}

export interface CommentUpdateRequest {
  content: string;
}

