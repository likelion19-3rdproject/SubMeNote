'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { notificationApi } from '@/src/api/notificationApi';
import { NotificationResponseDto, NotificationType, NotificationTargetType } from '@/src/types/notification';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import Button from '@/src/components/common/Button';
import Card from '@/src/components/common/Card';

export default function NotificationDetailPage() {
  const router = useRouter();
  const params = useParams();
  const notificationId = Number(params.notificationId);

  const [notification, setNotification] = useState<NotificationResponseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (notificationId) {
      fetchNotification();
    }
  }, [notificationId]);

  const fetchNotification = async () => {
    try {
      setLoading(true);
      const data = await notificationApi.getNotification(notificationId);
      setNotification(data);

      // 읽지 않은 알림이면 자동으로 읽음 처리
      if (!data.readAt) {
        await notificationApi.readNotifications([notificationId]);
      }
    } catch (error: any) {
      console.error('알림 조회 실패:', error);
      if (error.response?.status === 404) {
        alert('알림을 찾을 수 없습니다.');
      } else if (error.response?.status === 403) {
        alert('알림에 접근할 수 없습니다.');
      } else {
        alert('알림 조회에 실패했습니다.');
      }
      router.push('/notifications');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm('이 알림을 삭제하시겠습니까?')) return;

    try {
      setDeleting(true);
      await notificationApi.deleteNotification(notificationId);
      alert('알림이 삭제되었습니다.');
      router.push('/notifications');
    } catch (error) {
      console.error('알림 삭제 실패:', error);
      alert('알림 삭제에 실패했습니다.');
      setDeleting(false);
    }
  };

  const handleGoToTarget = () => {
    if (!notification) return;

    const { notificationTargetType, targetId } = notification;

    switch (notificationTargetType) {
      case NotificationTargetType.POST:
        if (targetId) router.push(`/posts/${targetId}`);
        break;
      case NotificationTargetType.SUBSCRIBE:
        router.push('/me/subscriptions');
        break;
      default:
        alert('이동할 페이지가 없습니다.');
        break;
    }
  };

  const getTypeIcon = () => {
    if (!notification) return '🔔';
    
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

  const getTypeName = () => {
    if (!notification) return '';
    
    switch (notification.notificationType) {
      case NotificationType.COMMENT_CREATED:
        return '새 댓글';
      case NotificationType.POST_REPORTED:
        return '게시글 신고';
      case NotificationType.COMMENT_REPORTED:
        return '댓글 신고';
      case NotificationType.SUBSCRIBE_EXPIRE_SOON:
        return '구독 만료 예정';
      case NotificationType.ANNOUNCEMENT:
        return '공지사항';
      default:
        return '알림';
    }
  };

  const canNavigateToTarget = () => {
    if (!notification) return false;
    return (
      (notification.notificationTargetType === NotificationTargetType.POST && notification.targetId) ||
      notification.notificationTargetType === NotificationTargetType.SUBSCRIBE
    );
  };

  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 py-8">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  if (!notification) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-8">
        <button
          onClick={() => router.push('/notifications')}
          className="text-gray-400 hover:text-white text-sm mb-6 flex items-center gap-1"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          알림 목록으로 돌아가기
        </button>

        <Card className="overflow-hidden">
          <div className="p-6 relative pb-6 mb-6">
            {/* 그라데이션 구분선 */}
            <div className="absolute bottom-0 left-0 right-0 h-[2px] bg-gradient-to-r from-transparent via-purple-400/50 to-transparent"></div>
            <div className="absolute bottom-0 left-0 right-0 h-[2px] bg-purple-400/30 blur-sm"></div>
            <div className="flex items-start gap-4">
              <div className="text-4xl flex-shrink-0">{getTypeIcon()}</div>
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    {getTypeName()}
                  </span>
                  {!notification.readAt && (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                      읽지 않음
                    </span>
                  )}
                </div>
                <h1 className="text-2xl font-bold text-white mb-2">
                  {notification.title}
                </h1>
                <p className="text-sm text-gray-500">
                  {formatDateTime(notification.createdAt)}
                </p>
              </div>
            </div>
          </div>

          <div className="p-6">
            <div className="mb-6">
              <h2 className="text-sm font-semibold text-gray-200 mb-2">알림 내용</h2>
              <p className="text-white whitespace-pre-wrap leading-relaxed">
                {notification.message}
              </p>
            </div>

            {notification.readAt && (
              <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                <p className="text-sm text-gray-600">
                  <span className="font-medium">읽은 시간:</span> {formatDateTime(notification.readAt)}
                </p>
              </div>
            )}

            <div className="flex flex-wrap gap-3">
              {canNavigateToTarget() && (
                <Button
                  variant="primary"
                  onClick={handleGoToTarget}
                  className="flex-1 sm:flex-initial"
                >
                  {notification.notificationTargetType === NotificationTargetType.POST ? '게시글 보기' : '구독 관리'}
                </Button>
              )}
              <Button
                variant="secondary"
                onClick={() => router.push('/notifications')}
                className="flex-1 sm:flex-initial"
              >
                목록으로
              </Button>
              <Button
                variant="danger"
                onClick={handleDelete}
                disabled={deleting}
                className="flex-1 sm:flex-initial"
              >
                {deleting ? '삭제 중...' : '삭제'}
              </Button>
            </div>
          </div>
        </div>

        {notification.notificationType === NotificationType.ANNOUNCEMENT && (
          <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex gap-3">
              <div className="flex-shrink-0">
                <svg className="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                </svg>
              </div>
              <div>
                <h3 className="text-sm font-medium text-blue-800 mb-1">관리자 공지사항</h3>
                <p className="text-sm text-blue-700">
                  이 알림은 관리자가 전체 사용자에게 발송한 공지사항입니다.
                </p>
              </div>
            </div>
          </div>
        )}

        {(notification.notificationType === NotificationType.POST_REPORTED || 
          notification.notificationType === NotificationType.COMMENT_REPORTED) && (
          <div className="mt-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex gap-3">
              <div className="flex-shrink-0">
                <svg className="w-5 h-5 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              </div>
              <div>
                <h3 className="text-sm font-medium text-yellow-800 mb-1">신고 알림</h3>
                <p className="text-sm text-yellow-700">
                  회원님의 콘텐츠가 신고되었습니다. 커뮤니티 가이드라인을 확인해주세요.
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
