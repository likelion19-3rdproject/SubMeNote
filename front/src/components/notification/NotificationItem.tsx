'use client';

import { NotificationResponseDto, NotificationType, NotificationTargetType } from '@/src/types/notification';
import { useRouter } from 'next/navigation';

interface NotificationItemProps {
  notification: NotificationResponseDto;
  onDelete?: (id: number) => void;
  onClick?: (notification: NotificationResponseDto) => void;
  compact?: boolean;
}

export default function NotificationItem({
  notification,
  onDelete,
  onClick,
  compact = false,
}: NotificationItemProps) {
  const router = useRouter();

  const handleClick = () => {
    if (onClick) {
      onClick(notification);
    } else {
      // 기본 동작: 타겟으로 이동
      navigateToTarget();
    }
  };

  const navigateToTarget = () => {
    const { notificationTargetType, targetId } = notification;

    switch (notificationTargetType) {
      case NotificationTargetType.POST:
        if (targetId) router.push(`/posts/${targetId}`);
        break;
      case NotificationTargetType.COMMENT:
        // 댓글 알림의 경우 targetId가 댓글 ID이므로
        // 백엔드에서 개별 댓글 조회 API가 없어 게시글로 직접 이동 불가
        // 알림 메시지에 정보가 충분히 담겨있으므로 이동하지 않음
        break;
      case NotificationTargetType.SUBSCRIBE:
        router.push('/me/subscriptions');
        break;
      case NotificationTargetType.NONE:
        // 공지사항 등은 클릭해도 이동하지 않음
        break;
      default:
        break;
    }
  };

  const getTypeIcon = () => {
    switch (notification.notificationType) {
      case NotificationType.COMMENT_CREATED:
        return '💬';
      case NotificationType.POST_REPORTED:
      case NotificationType.COMMENT_REPORTED:
        return '⚠️';
      case NotificationType.SUBSCRIBE_EXPIRE_SOON:
        return '⏰';
      case NotificationType.ANNOUNCEMENT:
        return '📢';
      default:
        return '🔔';
    }
  };

  const getTimeAgo = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) return '방금 전';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전`;
    if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}일 전`;
    return date.toLocaleDateString('ko-KR');
  };

  const isUnread = !notification.readAt;

  return (
    <div
      className={`
        ${compact ? 'p-3' : 'p-4'}
        ${isUnread ? 'bg-blue-50' : 'bg-white'}
        hover:bg-gray-50 cursor-pointer transition-colors
        ${!compact && 'border-b border-gray-100'}
      `}
      onClick={handleClick}
    >
      <div className="flex items-start gap-3">
        <div className="text-2xl flex-shrink-0">{getTypeIcon()}</div>
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div className="flex-1">
              <h4 className={`text-sm ${isUnread ? 'font-semibold' : 'font-normal'} text-white`}>
                {notification.title}
              </h4>
              <p className={`text-sm text-gray-600 mt-1 ${compact && 'line-clamp-2'}`}>
                {notification.message}
              </p>
            </div>
            {!compact && onDelete && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onDelete(notification.id);
                }}
                className="text-gray-400 hover:text-gray-600 flex-shrink-0"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>
          <div className="flex items-center gap-2 mt-2">
            <span className="text-xs text-gray-500">{getTimeAgo(notification.createdAt)}</span>
            {isUnread && (
              <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
