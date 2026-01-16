/** @type {import('next').NextConfig} */
const nextConfig = {
  /* config options here */
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
    NEXT_PUBLIC_TOSS_CLIENT_KEY: process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY || 'test_ck_4yKeq5bgrpLAXPeBzqK4rGX0lzW6',
    NEXT_PUBLIC_TOSS_REDIRECT_MODE: process.env.NEXT_PUBLIC_TOSS_REDIRECT_MODE || 'frontend',
  },
  output: 'standalone',
};

export default nextConfig;
