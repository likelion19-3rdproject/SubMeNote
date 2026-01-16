import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// 1. 로그인이 반드시 필요한 경로들 (이 경로와 하위 경로는 모두 보호됨)
const protectedPaths = [
  '/feed',
  '/me',       // 마이페이지 전체
  '/creator',  // 크리에이터 관리 (하위 경로: /creator/posts/new 포함)
  '/subscribe', // 구독/결제 로직
  '/pay',      // 결제 결과
];

// 2. 이미 로그인한 사용자가 접근하면 안 되는 경로 (로그인, 회원가입)
const authPaths = ['/login', '/signup'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const accessToken = request.cookies.get('accessToken');

  // [로직 1] 보호된 경로 접근 시: 토큰이 없으면 로그인 페이지로 리다이렉트
  // startsWith를 사용하여 '/me/orders', '/subscribe/1' 등 하위 경로도 모두 포함
  const isProtectedPath = protectedPaths.some((path) => 
    pathname === path || pathname.startsWith(`${path}/`)
  );

  if (isProtectedPath && !accessToken) {
    const loginUrl = new URL('/login', request.url);
    // 로그인 후 원래 가려던 페이지로 돌아오기 위해 callbackUrl 설정
    loginUrl.searchParams.set('callbackUrl', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // [로직 2] 로그인된 상태에서 로그인/회원가입 페이지 접근 시: 메인으로 리다이렉트
  if (accessToken && authPaths.includes(pathname)) {
    return NextResponse.redirect(new URL('/', request.url));
  }

  // 그 외 경로는 통과 (공개 페이지: /, /creators/[id], /posts/[id] 등)
  return NextResponse.next();
}

export const config = {
  // 미들웨어가 실행될 경로 패턴 (API, 정적 파일 등 제외)
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico|.*\\.svg|.*\\.png|.*\\.jpg).*)',
  ],
};