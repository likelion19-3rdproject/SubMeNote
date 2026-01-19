export enum NotificationType {
  COMMENT_CREATED = 'COMMENT_CREATED',
  COMMENT_REPORTED = 'COMMENT_REPORTED',
  POST_REPORTED = 'POST_REPORTED',
  SUBSCRIBE_EXPIRE_SOON = 'SUBSCRIBE_EXPIRE_SOON',
  ANNOUNCEMENT = 'ANNOUNCEMENT',
}

export enum NotificationTargetType {
  POST = 'POST',
  COMMENT = 'COMMENT',
  REPORT = 'REPORT',
  SUBSCRIBE = 'SUBSCRIBE',
  NONE = 'NONE',
}

export interface NotificationResponseDto {
  id: number;
  notificationType: NotificationType;
  notificationTargetType: NotificationTargetType;
  targetId: number | null;
  title: string;
  message: string;
  createdAt: string;
  readAt: string | null;
}

export interface NotificationReadRequest {
  notificationIds: number[];
}

export interface NotificationReadResponse {
  requested: number;
  updated: number;
}
