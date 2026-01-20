/** @type {import('next').NextConfig} */
const nextConfig = {
  /* config options here */
  env: {
    // NOTE: 기본값을 localhost로 두면 EC2 배포에서 브라우저가 loopback(127.0.0.1)으로 호출해
    // CORS/Private Network Access 에러가 발생할 수 있습니다.
    // 가능한 한 .env(.env.prod)에서 NEXT_PUBLIC_API_URL을 명시하세요.
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
    NEXT_PUBLIC_TOSS_CLIENT_KEY: process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY || 'test_ck_4yKeq5bgrpLAXPeBzqK4rGX0lzW6',
    NEXT_PUBLIC_TOSS_REDIRECT_MODE: process.env.NEXT_PUBLIC_TOSS_REDIRECT_MODE || 'frontend',
  },
  output: 'standalone',
};

export default nextConfig;
