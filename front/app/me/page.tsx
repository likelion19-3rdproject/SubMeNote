'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { userApi } from '@/src/api/userApi';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';

export default function MyPage() {
  const router = useRouter();
  const [isCreator, setIsCreator] = useState(false);
  const [userInfo, setUserInfo] = useState<{ nickname: string } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const user = await userApi.getMe();
        setIsCreator(user.roles.includes('ROLE_CREATOR'));
        setUserInfo({ nickname: user.nickname });
      } catch (error) {
        console.error('Failed to fetch user info:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchUserInfo();
  }, []);

  const handleDeleteAccount = async () => {
    if (!confirm('정말 회원탈퇴를 하시겠습니까?')) return;

    try {
      await userApi.deleteUser();
      router.push('/login');
      window.location.href = '/login'; // 쿠키 삭제를 위해 페이지 새로고침
    } catch (error: any) {
      alert(error.response?.data?.message || '회원탈퇴에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">마이페이지</h1>
      
      {/* 닉네임 표시 */}
      {userInfo && (
        <Card className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-2">닉네임</h2>
          <p className="text-gray-600">{userInfo.nickname}</p>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 일반 회원 메뉴 */}
        <Card>
          <h2 className="text-xl font-semibold text-gray-900 mb-4">일반 메뉴</h2>
          <div className="space-y-3">
            <Link href="/me/subscriptions">
              <Button variant="secondary" className="w-full">
                내가 구독한 크리에이터
              </Button>
            </Link>
            <Link href="/me/comments">
              <Button variant="secondary" className="w-full">
                내가 작성한 댓글
              </Button>
            </Link>
            <Link href="/me/orders">
              <Button variant="secondary" className="w-full">
                주문내역
              </Button>
            </Link>
            {!isCreator && (
              <Link href="/me/creator-application">
                <Button variant="secondary" className="w-full">
                  크리에이터 신청
                </Button>
              </Link>
            )}
          </div>
        </Card>

        {/* 크리에이터 메뉴 */}
        {isCreator && (
          <Card>
            <h2 className="text-xl font-semibold text-gray-900 mb-4">크리에이터 메뉴</h2>
            <div className="space-y-3">
              <Link href="/me/posts">
                <Button variant="secondary" className="w-full">
                  내 게시글
                </Button>
              </Link>
              <Link href="/me/account">
                <Button variant="secondary" className="w-full">
                  계좌관리
                </Button>
              </Link>
              <Link href="/me/settlements">
                <Button variant="secondary" className="w-full">
                  정산
                </Button>
              </Link>
              <Link href="/me/profile-image">
                <Button variant="secondary" className="w-full">
                  프로필 이미지 설정
                </Button>
              </Link>
            </div>
          </Card>
        )}

        {/* 회원탈퇴 */}
        <Card className="md:col-span-2">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">회원탈퇴</h2>
              <p className="text-sm text-gray-500 mt-1">계정을 삭제하면 모든 데이터가 삭제됩니다.</p>
            </div>
            <Button variant="danger" onClick={handleDeleteAccount}>
              회원탈퇴
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}

