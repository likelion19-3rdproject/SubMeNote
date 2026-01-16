'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { authApi } from '@/src/api/authApi';
import { emailApi } from '@/src/api/emailApi';
import Input from '@/src/components/common/Input';
import Button from '@/src/components/common/Button';
import ErrorState from '@/src/components/common/ErrorState';

export default function SignupPage() {
  const router = useRouter();
  const [step, setStep] = useState<'email' | 'verify' | 'info'>('email');
  const [email, setEmail] = useState('');
  const [authCode, setAuthCode] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [role, setRole] = useState<'ROLE_USER' | 'ROLE_CREATOR'>('ROLE_USER');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [emailSent, setEmailSent] = useState(false);
  const [emailVerified, setEmailVerified] = useState(false);
  const [nicknameAvailable, setNicknameAvailable] = useState<boolean | null>(null);

  const handleSendEmail = async () => {
    setError(null);
    setLoading(true);
    try {
      await emailApi.send({ email });
      setEmailSent(true);
    } catch (err: any) {
      setError(err.response?.data?.message || '이메일 전송에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleResendEmail = async () => {
    setError(null);
    setLoading(true);
    try {
      await emailApi.resend({ email });
      setEmailSent(true);
    } catch (err: any) {
      setError(err.response?.data?.message || '이메일 재전송에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyEmail = async () => {
    setError(null);
    setLoading(true);
    try {
      const result = await emailApi.verify({ email, authCode });
      if (result.verified) {
        setEmailVerified(true);
        setStep('info');
      } else {
        setError('인증 코드가 올바르지 않습니다.');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || '이메일 인증에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCheckNickname = async () => {
    if (!nickname) return;
    setError(null);
    setLoading(true);
    try {
      const result = await authApi.checkDuplication(nickname);
      setNicknameAvailable(result.available);
      if (!result.available) {
        setError('이미 사용 중인 닉네임입니다.');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || '닉네임 확인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }
    if (nicknameAvailable !== true) {
      setError('닉네임 중복 확인을 해주세요.');
      return;
    }

    setError(null);
    setLoading(true);
    try {
      await authApi.signup({ email, nickname, password, role });
      router.push('/login');
    } catch (err: any) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">회원가입</h2>
        </div>

        {error && <ErrorState message={error} />}

        {step === 'email' && (
          <div className="space-y-4">
            <Input
              label="이메일"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading || emailSent}
            />
            {!emailSent ? (
              <Button onClick={handleSendEmail} disabled={loading || !email}>
                인증 이메일 전송
              </Button>
            ) : (
              <div className="space-y-2">
                <p className="text-sm text-green-600">인증 이메일이 전송되었습니다.</p>
                <Button onClick={handleResendEmail} variant="secondary" disabled={loading}>
                  재전송
                </Button>
                <Button onClick={() => setStep('verify')} disabled={loading}>
                  인증 코드 입력하기
                </Button>
              </div>
            )}
          </div>
        )}

        {step === 'verify' && (
          <div className="space-y-4">
            <Input
              label="인증 코드"
              value={authCode}
              onChange={(e) => setAuthCode(e.target.value)}
              required
              disabled={loading}
            />
            <div className="flex space-x-2">
              <Button onClick={handleVerifyEmail} disabled={loading || !authCode} className="flex-1">
                인증하기
              </Button>
              <Button onClick={handleResendEmail} variant="secondary" disabled={loading}>
                재전송
              </Button>
            </div>
          </div>
        )}

        {step === 'info' && (
          <form className="space-y-4" onSubmit={handleSignup}>
            <Input
              label="닉네임"
              value={nickname}
              onChange={(e) => {
                setNickname(e.target.value);
                setNicknameAvailable(null);
              }}
              required
              disabled={loading}
            />
            <Button
              type="button"
              onClick={handleCheckNickname}
              variant="secondary"
              disabled={loading || !nickname}
            >
              중복 확인
            </Button>
            {nicknameAvailable === true && (
              <p className="text-sm text-green-600">사용 가능한 닉네임입니다.</p>
            )}

            <Input
              label="비밀번호"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
            <Input
              label="비밀번호 확인"
              type="password"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
              required
              disabled={loading}
            />

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">회원 유형</label>
              <div className="space-y-2">
                <label className="flex items-center">
                  <input
                    type="radio"
                    value="ROLE_USER"
                    checked={role === 'ROLE_USER'}
                    onChange={(e) => setRole(e.target.value as 'ROLE_USER' | 'ROLE_CREATOR')}
                    className="mr-2"
                  />
                  일반 회원
                </label>
                <label className="flex items-center">
                  <input
                    type="radio"
                    value="ROLE_CREATOR"
                    checked={role === 'ROLE_CREATOR'}
                    onChange={(e) => setRole(e.target.value as 'ROLE_USER' | 'ROLE_CREATOR')}
                    className="mr-2"
                  />
                  크리에이터
                </label>
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? '가입 중...' : '회원가입'}
            </Button>
          </form>
        )}

        <div className="flex justify-center space-x-4 text-sm">
          <button
            type="button"
            onClick={() => router.push('/')}
            className="text-gray-600 hover:text-gray-800"
          >
            메인으로
          </button>
          <span className="text-gray-300">|</span>
          <button
            type="button"
            onClick={() => router.push('/login')}
            className="text-blue-600 hover:text-blue-800"
          >
            로그인으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
}

