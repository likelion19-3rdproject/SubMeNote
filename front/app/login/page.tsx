'use client';

import { Suspense, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { authApi } from '@/src/api/authApi';
import Input from '@/src/components/common/Input';
import Button from '@/src/components/common/Button';
import ErrorState from '@/src/components/common/ErrorState';

function LoginForm() {
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
    <div className="min-h-screen flex items-center justify-center py-12 px-6">
      <div className="max-w-md w-full animate-fade-in-scale">
        <div className="glass p-10 rounded-3xl border border-purple-500/30 neon-glow">
          <div className="text-center mb-10">
            <div className="text-7xl mb-6 animate-float">⚡</div>
            <h2 className="text-5xl font-black gradient-text mb-3 neon-text">
              환영합니다!
            </h2>
            <p className="text-gray-400 text-lg">
              SNS Service에 로그인하세요
            </p>
          </div>
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
                placeholder="your@email.com"
              />
              <Input
                label="비밀번호"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={loading}
                placeholder="••••••••"
              />
            </div>
            <div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? "로그인 중..." : "🚀 로그인"}
              </Button>
            </div>
            <div className="flex justify-center space-x-4 text-sm text-gray-400 pt-6 border-t border-purple-500/20">
              <button
                type="button"
                onClick={() => router.push("/")}
                className="hover:text-white transition-colors font-bold relative group"
              >
                <span className="relative z-10">메인으로</span>
                <div className="absolute inset-0 bg-gradient-to-r from-purple-600/0 via-purple-600/20 to-purple-600/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700 rounded"></div>
              </button>
              <span className="text-purple-500/50">|</span>
              <button
                type="button"
                onClick={() => router.push("/signup")}
                className="hover:text-white transition-colors font-bold relative group"
              >
                <span className="relative z-10">회원가입</span>
                <div className="absolute inset-0 bg-gradient-to-r from-pink-600/0 via-pink-600/20 to-pink-600/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700 rounded"></div>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    }>
      <LoginForm />
    </Suspense>
  );
}
