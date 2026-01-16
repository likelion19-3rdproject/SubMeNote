export type ReportType = 'POST' | 'COMMENT';

export interface ReportCreateRequest {
  targetId: number;
  type: ReportType;
  customReason?: string;
}

export interface ReportResponse {
  id: number;
  userId: number;
  postId: number | null;
  commentId: number | null;
  type: ReportType;
  customReason: string | null;
}

export type PostReportStatus = 'NORMAL' | 'HIDDEN';
export type CommentReportStatus = 'NORMAL' | 'HIDDEN';

export interface HiddenPostResponse {
  postId: number;
  userId: number;
  nickName: string;
  title: string;
  visibility: 'PUBLIC' | 'SUBSCRIBERS_ONLY';
  status: PostReportStatus;
  createdAt: string;
}

export interface HiddenCommentResponse {
  commentId: number;
  userId: number;
  nickName: string;
  postId: number;
  content: string;
  status: CommentReportStatus;
  createdAt: string;
}

export interface ReportRestoreRequest {
  targetId: number;
  type: ReportType;
}

export interface ReportDeleteRequest {
  targetId: number;
  type: ReportType;
}
