'use client';

import { useEffect, useState, useCallback } from 'react';
import type { MouseEvent } from 'react';
import { useRouter } from 'next/navigation';
import { notificationApi } from '@/src/api/notificationApi';
import type { NotificationResponseDto } from '@/src/types/notification';
import NotificationItem from '@/src/components/notification/NotificationItem';
import Pagination from '@/src/components/common/Pagination';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import Card from '@/src/components/common/Card';

export default function NotificationsPage() {
  const router = useRouter();
  const [notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());

  const fetchNotifications = useCallback(async (page: number): Promise<void> => {
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
  }, []);

  useEffect(() => {
    fetchNotifications(currentPage);
  }, [currentPage, fetchNotifications]);

  const handleNotificationClick = (notification: NotificationResponseDto): void => {
    router.push(`/notifications/${notification.id}`);
  };

  const handleDelete = async (id: number): Promise<void> => {
    if (!confirm('이 알림을 삭제하시겠습니까?')) return;

    try {
      await notificationApi.deleteNotification(id);
      fetchNotifications(currentPage);
    } catch (error) {
      console.error('알림 삭제 실패:', error);
      alert('알림 삭제에 실패했습니다.');
    }
  };

  const handleSelectToggle = (id: number): void => {
    const newSelected = new Set(selectedIds);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedIds(newSelected);
  };

  const handleSelectAll = (): void => {
    if (selectedIds.size === notifications.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(notifications.map((n: NotificationResponseDto) => n.id)));
    }
  };

  const handleDeleteSelected = async (): Promise<void> => {
    if (selectedIds.size === 0) {
      alert('삭제할 알림을 선택해주세요.');
      return;
    }

    if (!confirm(`선택한 ${selectedIds.size}개의 알림을 삭제하시겠습니까?`)) return;

    try {
      const idsArray: number[] = Array.from(selectedIds);
      await Promise.all(
        idsArray.map((id: number) => notificationApi.deleteNotification(id))
      );
      fetchNotifications(currentPage);
    } catch (error) {
      console.error('알림 삭제 실패:', error);
      alert('알림 삭제에 실패했습니다.');
    }
  };

  const handleReadAll = async (): Promise<void> => {
    const unreadIds: number[] = notifications
      .filter((n: NotificationResponseDto) => !n.readAt)
      .map((n: NotificationResponseDto) => n.id);
    
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

  const handleReadSelected = async (): Promise<void> => {
    if (selectedIds.size === 0) {
      alert('읽음 처리할 알림을 선택해주세요.');
      return;
    }

    try {
      const idsArray: number[] = Array.from(selectedIds);
      await notificationApi.readNotifications(idsArray);
      fetchNotifications(currentPage);
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
      alert('알림 읽음 처리에 실패했습니다.');
    }
  };

  const unreadCount: number = notifications.filter((n: NotificationResponseDto) => !n.readAt).length;

  if (loading && notifications.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-16">
        <LoadingSpinner />
      </div>
    );
  }

  const handleCheckboxClick = (e: MouseEvent<HTMLInputElement>): void => {
    e.stopPropagation();
  };

  return (
    <div className="max-w-4xl mx-auto px-6 py-16">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-3">알림</h1>
        {unreadCount > 0 && (
          <p className="text-sm text-gray-500">읽지 않은 알림 {unreadCount}개</p>
        )}
      </div>

      {notifications.length > 0 && (
        <Card className="mb-6">
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
              className="text-sm text-gray-700 hover:text-gray-900 font-medium px-4 py-2 rounded-xl hover:bg-gray-50 transition-colors"
            >
              모두 읽음
            </button>

            {selectedIds.size > 0 && (
              <>
                <button
                  onClick={handleReadSelected}
                  className="text-sm text-gray-700 hover:text-gray-900 font-medium px-4 py-2 rounded-xl hover:bg-gray-50 transition-colors"
                >
                  선택 읽음
                </button>
                <button
                  onClick={handleDeleteSelected}
                  className="text-sm text-red-600 hover:text-red-700 font-medium px-4 py-2 rounded-xl hover:bg-red-50 transition-colors"
                >
                  선택 삭제 ({selectedIds.size})
                </button>
              </>
            )}
          </div>
        </Card>
      )}

      {notifications.length === 0 ? (
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-12 text-center">
          <div className="text-6xl mb-4">🔔</div>
          <p className="text-gray-500">알림이 없습니다</p>
        </div>
      ) : (
        <div className="grid gap-4">
          {notifications.map((notification: NotificationResponseDto) => (
            <div key={notification.id} className="flex items-start">
              <Card className="flex-1">
                <div className="flex items-start gap-4">
                  <label className="flex items-center cursor-pointer pt-1">
                    <input
                      type="checkbox"
                      checked={selectedIds.has(notification.id)}
                      onChange={() => handleSelectToggle(notification.id)}
                      className="w-4 h-4 text-[#FFC837] rounded border-gray-300 focus:ring-[#FFC837]"
                      onClick={handleCheckboxClick}
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
              </Card>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="mt-6">
          <Pagination
            currentPage={currentPage + 1}
            totalPages={totalPages}
            onPageChange={(page: number) => setCurrentPage(page - 1)}
          />
        </div>
      )}
    </div>
  );
}
