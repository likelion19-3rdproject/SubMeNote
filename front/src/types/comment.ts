export interface CommentResponseDto {
  id: number;
  content: string;
  postId: number;
  userId: number;
  userNickname: string;
  createdAt: string;
  updatedAt: string;
}

export interface CommentCreateRequest {
  content: string;
}

export interface CommentUpdateRequest {
  content: string;
}

