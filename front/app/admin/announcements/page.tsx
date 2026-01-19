'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { userApi } from '@/src/api/userApi';
import { adminApi } from '@/src/api/adminApi';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import Textarea from '@/src/components/common/Textarea';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';

export default function AnnouncementsPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);

  useEffect(() => {
    const checkAdmin = async () => {
      try {
        const user = await userApi.getMe();
        const hasAdminRole = user.roles.includes('ROLE_ADMIN');
        setIsAdmin(hasAdminRole);
        
        if (!hasAdminRole) {
          alert('관리자만 접근할 수 있습니다.');
          router.push('/');
        }
      } catch (error) {
        console.error('Failed to fetch user info:', error);
        router.push('/login');
      } finally {
        setLoading(false);
      }
    };
    checkAdmin();
  }, [router]);

  const handleSend = async () => {
    if (!message.trim()) {
      alert('공지사항 내용을 입력해주세요.');
      return;
    }

    if (!confirm('전체 사용자에게 공지사항을 발송하시겠습니까?')) {
      return;
    }

    try {
      setSending(true);
      await adminApi.sendAnnouncement(message);
      alert('공지사항이 성공적으로 발송되었습니다.');
      setMessage('');
      // 공지사항 목록 페이지로 이동
      router.push('/admin/announcements/list');
    } catch (error: any) {
      console.error('공지사항 발송 실패:', error);
      alert(error.response?.data?.message || '공지사항 발송에 실패했습니다.');
    } finally {
      setSending(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  if (!isAdmin) {
    return null;
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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
        <div className="flex justify-between items-center mb-2">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">전체 공지사항 발송</h1>
            <p className="text-gray-600 mt-2">
              모든 사용자에게 알림으로 공지사항을 발송합니다.
            </p>
          </div>
          <Button
            variant="secondary"
            onClick={() => router.push('/admin/announcements/list')}
          >
            공지사항 목록 보기
          </Button>
        </div>
      </div>

      <Card>
        <div className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              공지사항 내용 <span className="text-red-500">*</span>
            </label>
            <Textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="전체 사용자에게 전달할 공지사항 내용을 입력하세요.&#10;&#10;예시:&#10;- 시스템 점검 안내&#10;- 새로운 기능 출시 안내&#10;- 중요한 공지사항 등"
              rows={10}
              className="w-full"
            />
            <p className="text-sm text-gray-500 mt-2">
              현재 입력: {message.length}자
            </p>
          </div>

          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex gap-3">
              <div className="flex-shrink-0">
                <svg className="w-5 h-5 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              </div>
              <div>
                <h3 className="text-sm font-medium text-yellow-800 mb-1">주의사항</h3>
                <ul className="text-sm text-yellow-700 space-y-1 list-disc list-inside">
                  <li>모든 사용자에게 알림이 발송됩니다.</li>
                  <li>발송 후 취소할 수 없습니다.</li>
                  <li>신중하게 내용을 확인한 후 발송해주세요.</li>
                </ul>
              </div>
            </div>
          </div>

          <div className="flex gap-3">
            <Button
              variant="secondary"
              onClick={() => router.push('/admin')}
              disabled={sending}
              className="flex-1"
            >
              취소
            </Button>
            <Button
              variant="primary"
              onClick={handleSend}
              disabled={sending || !message.trim()}
              className="flex-1"
            >
              {sending ? '발송 중...' : '공지사항 발송'}
            </Button>
          </div>
        </div>
      </Card>

      <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
        <h2 className="text-lg font-semibold text-blue-900 mb-3">💡 사용 팁</h2>
        <ul className="text-sm text-blue-800 space-y-2">
          <li className="flex gap-2">
            <span className="flex-shrink-0">•</span>
            <span>긴급한 시스템 점검이나 중요한 업데이트 사항을 전달할 때 사용하세요.</span>
          </li>
          <li className="flex gap-2">
            <span className="flex-shrink-0">•</span>
            <span>명확하고 간결한 문구로 작성하여 사용자가 쉽게 이해할 수 있도록 하세요.</span>
          </li>
          <li className="flex gap-2">
            <span className="flex-shrink-0">•</span>
            <span>발송된 공지사항은 사용자의 알림함에 표시됩니다.</span>
          </li>
        </ul>
      </div>
    </div>
  );
}
