'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { userApi } from '@/src/api/userApi';
import { adminApi } from '@/src/api/adminApi';
import { AnnouncementResponse } from '@/src/types/announcement';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';

export default function AnnouncementListPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);
  const [announcements, setAnnouncements] = useState<Page<AnnouncementResponse> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  useEffect(() => {
    const checkAdmin = async () => {
      try {
        const user = await userApi.getMe();
        const hasAdminRole = user.roles.includes('ROLE_ADMIN');
        setIsAdmin(hasAdminRole);
        
        if (!hasAdminRole) {
          alert('관리자만 접근할 수 있습니다.');
          router.push('/');
          return;
        }

        await loadAnnouncements(0);
      } catch (error) {
        console.error('Failed to fetch user info:', error);
        router.push('/login');
      } finally {
        setLoading(false);
      }
    };
    checkAdmin();
  }, [router]);

  const loadAnnouncements = async (page: number) => {
    try {
      setLoading(true);
      const data = await adminApi.getAnnouncementList(page, pageSize);
      setAnnouncements(data);
      setCurrentPage(page);
    } catch (error: any) {
      console.error('Failed to fetch announcements:', error);
      alert(error.response?.data?.message || '공지사항 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !announcements) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  if (!isAdmin) {
    return null;
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-6">
        <button
          onClick={() => router.push('/admin')}
          className="text-gray-600 hover:text-gray-900 text-sm mb-4 flex items-center gap-1"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          관리자 센터로 돌아가기
        </button>
        <div className="flex justify-between items-center mb-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">공지사항 목록</h1>
            <p className="text-gray-600 mt-2">
              발송된 전체 공지사항 내역을 확인할 수 있습니다.
            </p>
          </div>
          <Button
            variant="primary"
            onClick={() => router.push('/admin/announcements')}
          >
            새 공지사항 발송
          </Button>
        </div>
      </div>

      {announcements && announcements.content.length > 0 ? (
        <>
          <div className="space-y-4">
            {announcements.content.map((announcement, index) => (
              <Card key={`${announcement.createdAt}-${index}`}>
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-blue-600 font-semibold">📢 공지사항</span>
                      <span className="text-sm text-gray-500">
                        {new Date(announcement.createdAt).toLocaleString('ko-KR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                    </div>
                    <p className="text-gray-800 whitespace-pre-wrap leading-relaxed">
                      {announcement.message}
                    </p>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {/* 페이지네이션 */}
          {announcements.totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              <Button
                variant="secondary"
                onClick={() => loadAnnouncements(currentPage - 1)}
                disabled={currentPage === 0 || loading}
              >
                이전
              </Button>
              <span className="text-sm text-gray-600">
                {currentPage + 1} / {announcements.totalPages}
              </span>
              <Button
                variant="secondary"
                onClick={() => loadAnnouncements(currentPage + 1)}
                disabled={currentPage >= announcements.totalPages - 1 || loading}
              >
                다음
              </Button>
            </div>
          )}
        </>
      ) : (
        <Card>
          <div className="text-center py-12">
            <p className="text-gray-500 mb-4">발송된 공지사항이 없습니다.</p>
            <Button
              variant="primary"
              onClick={() => router.push('/admin/announcements')}
            >
              첫 공지사항 발송하기
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
}
