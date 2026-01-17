export interface CommentResponseDto {
  id: number;
  content: string;
  postId: number;
  postTitle: string;
  userId: number;
  nickname: string;
  parentId: number | null;
  children: CommentResponseDto[];
  createdAt: string;
  updatedAt: string;
}

export interface CommentCreateRequest {
  content: string;
  parentId?: number | null;
}

export interface CommentUpdateRequest {
  content: string;
}

