'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { userApi } from '@/src/api/userApi';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';

export default function AdminPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);

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

  if (loading) {
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
      <h1 className="text-3xl font-bold text-gray-900 mb-8">관리자 센터</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 크리에이터 신청 관리 */}
        <Card>
          <h2 className="text-xl font-semibold text-gray-900 mb-4">크리에이터 신청 관리</h2>
          <p className="text-gray-600 mb-4">
            대기 중인 크리에이터 신청을 승인하거나 거절할 수 있습니다.
          </p>
          <Link href="/admin/creator-applications">
            <Button variant="primary" className="w-full">
              신청 목록 보기
            </Button>
          </Link>
        </Card>

        {/* 신고 관리 - 게시글 */}
        <Card>
          <h2 className="text-xl font-semibold text-gray-900 mb-4">게시글 신고 관리</h2>
          <p className="text-gray-600 mb-4">
            신고된 게시글을 확인하고 삭제하거나 복구할 수 있습니다.
          </p>
          <Link href="/admin/reports/posts">
            <Button variant="primary" className="w-full">
              신고 목록 보기
            </Button>
          </Link>
        </Card>

        {/* 신고 관리 - 댓글 */}
        <Card>
          <h2 className="text-xl font-semibold text-gray-900 mb-4">댓글 신고 관리</h2>
          <p className="text-gray-600 mb-4">
            신고된 댓글을 확인하고 삭제하거나 복구할 수 있습니다.
          </p>
          <Link href="/admin/reports/comments">
            <Button variant="primary" className="w-full">
              신고 목록 보기
            </Button>
          </Link>
        </Card>
      </div>
    </div>
  );
}
