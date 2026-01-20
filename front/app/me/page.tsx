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
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in-scale">
      <h1 className="text-4xl font-black text-white mb-10"><span>👤</span> <span className="gradient-text">마이페이지</span></h1>
      
      {/* 닉네임 표시 */}
      {userInfo && (
        <Card className="mb-8 border-2 border-purple-500/30">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-gradient-to-r from-purple-600 to-pink-600 flex items-center justify-center text-white font-black text-2xl neon-glow">
              {userInfo.nickname.charAt(0).toUpperCase()}
            </div>
            <div>
              <h2 className="text-sm font-bold text-gray-400 mb-1">닉네임</h2>
              <p className="text-2xl font-black text-white">{userInfo.nickname}</p>
            </div>
          </div>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 일반 회원 메뉴 */}
        <Card>
          <h2 className="text-2xl font-black text-white mb-6"><span>📋</span> <span className="gradient-text">일반 메뉴</span></h2>
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
            <h2 className="text-2xl font-black text-white mb-6"><span>🎨</span> <span className="gradient-text">크리에이터 메뉴</span></h2>
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
        <Card className="md:col-span-2 border-2 border-red-500/30">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-xl font-black text-white">⚠️ 회원탈퇴</h2>
              <p className="text-sm text-gray-400 mt-1 font-medium">계정을 삭제하면 모든 데이터가 삭제됩니다.</p>
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

