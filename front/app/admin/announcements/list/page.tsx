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

  if (loading && !announcements) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  if (!isAdmin) return null;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in-scale">
      <div className="mb-6">
        <button
          onClick={() => router.push('/admin')}
          className="text-gray-400 hover:text-white text-sm mb-4 flex items-center gap-1 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          관리자 센터로 돌아가기
        </button>

        <div className="mb-10">
          <h1 className="text-4xl font-black text-white mb-3">
            <span>📋</span> <span className="gradient-text">공지사항 목록</span>
          </h1>
          <p className="text-gray-400 text-lg">발송된 전체 공지사항 내역을 확인할 수 있습니다.</p>
        </div>

        <div className="flex justify-end mb-6">
          <Button variant="primary" onClick={() => router.push('/admin/announcements')}>
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
                      <span className="text-purple-400 font-semibold">📢 공지사항</span>
                      <span className="text-sm text-gray-400">
                        {new Date(announcement.createdAt).toLocaleString('ko-KR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                    </div>

                    <p className="text-white whitespace-pre-wrap leading-relaxed">
                      {announcement.message}
                    </p>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {announcements.totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              <Button
                variant="secondary"
                onClick={() => loadAnnouncements(currentPage - 1)}
                disabled={currentPage === 0 || loading}
              >
                이전
              </Button>

              <span className="text-sm text-gray-300">
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
            <div className="glass p-12 text-center rounded-2xl border border-purple-400/20 animate-fade-in-scale">
              <div className="text-7xl mb-6 animate-pulse">📭</div>
              <p className="text-gray-400 text-xl font-bold mb-6">발송된 공지사항이 없습니다.</p>

              <Button variant="primary" onClick={() => router.push('/admin/announcements')}>
                첫 공지사항 발송하기
              </Button>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
