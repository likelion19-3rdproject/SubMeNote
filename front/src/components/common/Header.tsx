'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { authApi } from '@/src/api/authApi';
import { subscribeApi } from '@/src/api/subscribeApi';
import { userApi } from '@/src/api/userApi';
import NotificationBell from '@/src/components/notification/NotificationBell';

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isCreator, setIsCreator] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    // 로그인/회원가입 페이지에서는 인증 체크를 하지 않음 (무한 리다이렉트 방지)
    const isAuthPage = pathname === '/login' || pathname === '/signup';
    if (isAuthPage) {
      setIsLoggedIn(false);
      setIsCreator(false);
      setIsAdmin(false);
      setIsLoading(false);
      return;
    }

    const checkAuthStatus = async () => {
      try {
        setIsLoading(true);
        // 사용자 정보 API로 로그인 상태 및 크리에이터 여부 확인
        const user = await userApi.getMe();
        // 컴포넌트가 마운트된 상태에서만 상태 업데이트
        if (isMounted) {
          setIsLoggedIn(true);
          setIsCreator(user.roles.includes('ROLE_CREATOR'));
          setIsAdmin(user.roles.includes('ROLE_ADMIN'));
        }
      } catch (error: any) {
        // 모든 에러는 비로그인 상태로 처리
        // (401, 네트워크 에러, 기타 에러 모두 포함)
        if (isMounted) {
          setIsLoggedIn(false);
          setIsCreator(false);
          setIsAdmin(false);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    checkAuthStatus();

    return () => {
      isMounted = false;
    };
  }, [pathname]); // pathname이 변경될 때마다 확인 (페이지 이동 시)

  const handleLogout = async () => {
    try {
      await authApi.logout();
      setIsLoggedIn(false);
      setIsCreator(false);
      setIsAdmin(false);
      router.push('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  // 로그인 페이지나 회원가입 페이지에서는 헤더를 간소화하여 표시 (로고만)
  if (pathname === '/login' || pathname === '/signup') {
    return (
      <header className="bg-white border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-6">
          <div className="flex justify-between items-center h-20">
            <div className="flex items-center">
              <Link href="/" className="text-2xl font-light text-gray-900 tracking-tight">
                SNS Service
              </Link>
            </div>
          </div>
        </div>
      </header>
    );
  }

  return (
    <header className="bg-white border-b border-gray-100 sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-6">
        <div className="flex justify-between items-center h-20">
          <div className="flex items-center">
            <Link
              href="/"
              className="text-2xl font-light text-gray-900 tracking-tight"
            >
              SNS Service
            </Link>
          </div>

          <nav className="flex items-center space-x-6">
            {isLoading ? (
              <div className="h-8 w-20"></div>
            ) : !isLoggedIn ? (
              <>
                <Link
                  href="/login"
                  className="text-gray-600 hover:text-gray-900 px-4 py-2 text-sm font-normal transition-colors"
                >
                  로그인
                </Link>
                <Link
                  href="/signup"
                  className="bg-gray-900 text-white hover:bg-gray-800 px-6 py-2.5 text-sm font-normal rounded-sm transition-colors"
                >
                  시작하기
                </Link>
              </>
            ) : isAdmin ? (
              // 관리자 메뉴
              <>
                <Link
                  href="/admin"
                  className="text-gray-600 hover:text-gray-900 px-4 py-2 text-sm font-normal transition-colors"
                >
                  관리자센터
                </Link>
                <NotificationBell />
                <button
                  onClick={handleLogout}
                  className="text-gray-600 hover:text-gray-900 px-4 py-2 text-sm font-normal transition-colors"
                >
                  로그아웃
                </button>
              </>
            ) : (
              // 일반 사용자/크리에이터 메뉴
              <>
                <Link
                  href="/feed"
                  className="text-gray-600 hover:text-gray-900 px-4 py-2 text-sm font-normal transition-colors"
                >
                  구독피드
                </Link>
                <Link
                  href="/me"
                  className="text-gray-600 hover:text-gray-900 px-4 py-2 text-sm font-normal transition-colors"
                >
                  마이페이지
                </Link>
                <NotificationBell />
                {isCreator && (
                  <Link
                    href="/creator/posts/new"
                    className="bg-gray-900 text-white hover:bg-gray-800 px-6 py-2.5 text-sm font-normal rounded-sm transition-colors"
                  >
                    글쓰기
                  </Link>
                )}
                <button
                  onClick={handleLogout}
                  className="text-gray-600 hover:text-gray-900 px-4 py-2 text-sm font-normal transition-colors"
                >
                  로그아웃
                </button>
              </>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
}

