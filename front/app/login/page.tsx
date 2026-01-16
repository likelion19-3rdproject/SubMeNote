'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { authApi } from '@/src/api/authApi';
import Input from '@/src/components/common/Input';
import Button from '@/src/components/common/Button';
import ErrorState from '@/src/components/common/ErrorState';

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await authApi.login({ email, password });
      // 쿠키는 자동으로 설정되므로 리다이렉트만 수행
      // callbackUrl이 있으면 해당 페이지로, 없으면 홈으로 이동
      const callbackUrl = searchParams.get('callbackUrl') || '/';
      window.location.href = callbackUrl;
    } catch (err: any) {
      setError(err.response?.data?.message || '로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-white py-12 px-6">
      <div className="max-w-md w-full">
        <h2 className="text-center text-3xl font-normal text-gray-900 mb-12">
          로그인
        </h2>
        <form className="space-y-6" onSubmit={handleSubmit}>
          {error && <ErrorState message={error} />}
          <div className="space-y-5">
            <Input
              label="이메일"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
              className="border-gray-200 focus:border-gray-400 rounded-sm"
            />
            <Input
              label="비밀번호"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
              className="border-gray-200 focus:border-gray-400 rounded-sm"
            />
          </div>
          <div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "로그인 중..." : "로그인"}
            </Button>
          </div>
          <div className="flex justify-center space-x-4 text-sm text-gray-500 pt-4">
            <button
              type="button"
              onClick={() => router.push("/")}
              className="hover:text-gray-900 transition-colors"
            >
              메인으로
            </button>
            <span className="text-gray-300">|</span>
            <button
              type="button"
              onClick={() => router.push("/signup")}
              className="hover:text-gray-900 transition-colors"
            >
              회원가입
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

