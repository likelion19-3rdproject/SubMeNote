'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { notificationApi } from '@/src/api/notificationApi';
import { NotificationResponseDto } from '@/src/types/notification';
import NotificationItem from '@/src/components/notification/NotificationItem';
import Pagination from '@/src/components/common/Pagination';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';

export default function NotificationsPage() {
  const router = useRouter();
  const [notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    fetchNotifications(currentPage);
  }, [currentPage]);

  const fetchNotifications = async (page: number) => {
    try {
      setLoading(true);
      const data = await notificationApi.getNotifications(page, 20);
      setNotifications(data.content);
      setTotalPages(data.totalPages);
      setSelectedIds(new Set());
    } catch (error) {
      console.error('알림 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleNotificationClick = async (notification: NotificationResponseDto) => {
    // 읽지 않은 알림이면 읽음 처리
    if (!notification.readAt) {
      try {
        await notificationApi.readNotifications([notification.id]);
        // 목록 다시 불러오기
        fetchNotifications(currentPage);
      } catch (error) {
        console.error('알림 읽음 처리 실패:', error);
      }
    }

    // 타겟으로 이동 (이동 가능한 경우만)
    if (notification.notificationTargetType === 'POST' && notification.targetId) {
      router.push(`/posts/${notification.targetId}`);
    } else if (notification.notificationTargetType === 'SUBSCRIBE') {
      router.push('/me/subscriptions');
    }
    // 댓글 알림이나 공지사항은 이동하지 않음 (메시지에 정보가 충분히 포함됨)
  };

  const handleDelete = async (id: number) => {
    if (!confirm('이 알림을 삭제하시겠습니까?')) return;

    try {
      await notificationApi.deleteNotification(id);
      fetchNotifications(currentPage);
    } catch (error) {
      console.error('알림 삭제 실패:', error);
      alert('알림 삭제에 실패했습니다.');
    }
  };

  const handleSelectToggle = (id: number) => {
    const newSelected = new Set(selectedIds);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedIds(newSelected);
  };

  const handleSelectAll = () => {
    if (selectedIds.size === notifications.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(notifications.map(n => n.id)));
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedIds.size === 0) {
      alert('삭제할 알림을 선택해주세요.');
      return;
    }

    if (!confirm(`선택한 ${selectedIds.size}개의 알림을 삭제하시겠습니까?`)) return;

    try {
      await Promise.all(
        Array.from(selectedIds).map(id => notificationApi.deleteNotification(id))
      );
      fetchNotifications(currentPage);
    } catch (error) {
      console.error('알림 삭제 실패:', error);
      alert('알림 삭제에 실패했습니다.');
    }
  };

  const handleReadAll = async () => {
    const unreadIds = notifications.filter(n => !n.readAt).map(n => n.id);
    
    if (unreadIds.length === 0) {
      alert('읽지 않은 알림이 없습니다.');
      return;
    }

    try {
      await notificationApi.readNotifications(unreadIds);
      fetchNotifications(currentPage);
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
      alert('알림 읽음 처리에 실패했습니다.');
    }
  };

  const handleReadSelected = async () => {
    if (selectedIds.size === 0) {
      alert('읽음 처리할 알림을 선택해주세요.');
      return;
    }

    try {
      await notificationApi.readNotifications(Array.from(selectedIds));
      fetchNotifications(currentPage);
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
      alert('알림 읽음 처리에 실패했습니다.');
    }
  };

  const unreadCount = notifications.filter(n => !n.readAt).length;

  if (loading && notifications.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 py-8">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">알림</h1>
          {unreadCount > 0 && (
            <p className="text-sm text-gray-600 mt-2">읽지 않은 알림 {unreadCount}개</p>
          )}
        </div>

        {notifications.length > 0 && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 mb-4 p-4">
            <div className="flex flex-wrap items-center gap-3">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={selectedIds.size === notifications.length && notifications.length > 0}
                  onChange={handleSelectAll}
                  className="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">전체 선택</span>
              </label>

              <div className="flex-1"></div>

              <button
                onClick={handleReadAll}
                className="text-sm text-blue-600 hover:text-blue-700 font-medium px-3 py-1.5 rounded hover:bg-blue-50"
              >
                모두 읽음
              </button>

              {selectedIds.size > 0 && (
                <>
                  <button
                    onClick={handleReadSelected}
                    className="text-sm text-blue-600 hover:text-blue-700 font-medium px-3 py-1.5 rounded hover:bg-blue-50"
                  >
                    선택 읽음
                  </button>
                  <button
                    onClick={handleDeleteSelected}
                    className="text-sm text-red-600 hover:text-red-700 font-medium px-3 py-1.5 rounded hover:bg-red-50"
                  >
                    선택 삭제 ({selectedIds.size})
                  </button>
                </>
              )}
            </div>
          </div>
        )}

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          {notifications.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 px-4">
              <div className="text-6xl mb-4">🔔</div>
              <p className="text-gray-500 text-lg">알림이 없습니다</p>
            </div>
          ) : (
            <div>
              {notifications.map((notification) => (
                <div key={notification.id} className="flex items-start border-b border-gray-100 last:border-b-0">
                  <label className="flex items-center px-4 py-4 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={selectedIds.has(notification.id)}
                      onChange={() => handleSelectToggle(notification.id)}
                      className="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
                      onClick={(e) => e.stopPropagation()}
                    />
                  </label>
                  <div className="flex-1">
                    <NotificationItem
                      notification={notification}
                      onDelete={handleDelete}
                      onClick={handleNotificationClick}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {totalPages > 1 && (
          <div className="mt-6">
            <Pagination
              currentPage={currentPage + 1}
              totalPages={totalPages}
              onPageChange={(page) => setCurrentPage(page - 1)}
            />
          </div>
        )}
      </div>
    </div>
  );
}
