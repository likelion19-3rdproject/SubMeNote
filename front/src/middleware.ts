import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// 인증이 필요한 경로 목록
const protectedPaths = [
  '/feed',
  '/me',
  '/creator',
  '/subscribe',
  '/pay',
];

// 인증이 필요 없는 경로 목록
const publicPaths = ['/login', '/signup', '/'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // 공개 경로는 통과
  if (publicPaths.some((path) => pathname === path || pathname.startsWith(path))) {
    return NextResponse.next();
  }

  // 보호된 경로는 쿠키 확인 (실제로는 백엔드 API로 인증 상태 확인 필요)
  // 현재는 쿠키 존재 여부만 확인 (accessToken 쿠키)
  const accessToken = request.cookies.get('accessToken');

  if (protectedPaths.some((path) => pathname.startsWith(path))) {
    if (!accessToken) {
      // 인증되지 않은 경우 로그인 페이지로 리다이렉트
      return NextResponse.redirect(new URL('/login', request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};

