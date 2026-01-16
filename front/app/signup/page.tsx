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
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [emailSent, setEmailSent] = useState(false);
  const [emailVerified, setEmailVerified] = useState(false);
  const [nicknameAvailable, setNicknameAvailable] = useState<boolean | null>(null);
  const [nicknameMessage, setNicknameMessage] = useState<string | null>(null);
  const [passwordValidation, setPasswordValidation] = useState({
    minLength: false,
    hasEnglish: false,
    hasNumber: false,
    hasSpecialChar: false,
  });
  const [passwordMatch, setPasswordMatch] = useState<boolean | null>(null);

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
    setNicknameMessage(null);
    setLoading(true);
    try {
      const result = await authApi.checkDuplication(nickname);
      setNicknameAvailable(result.available);
      if (result.available) {
        setNicknameMessage('사용 가능한 닉네임입니다.');
      } else {
        setNicknameMessage('이미 사용 중인 닉네임입니다.');
      }
    } catch (err: any) {
      setNicknameAvailable(null);
      setNicknameMessage(err.response?.data?.message || '닉네임 확인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 비밀번호 유효성 검사
  const validatePassword = (pwd: string) => {
    setPasswordValidation({
        minLength: pwd.length >= 8,
        hasEnglish: /[A-Za-z]/.test(pwd),
        hasNumber: /[0-9]/.test(pwd),
        hasSpecialChar: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pwd),
    });
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
      await authApi.signup({ email, nickname, password, role: 'ROLE_USER' });
      router.push('/');
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
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            회원가입
          </h2>
        </div>

        {step === "email" && (
          <div className="space-y-4 text-gray-900">
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
                <p className="text-sm text-green-600">
                  인증 이메일이 전송되었습니다.
                </p>
                <Button
                  onClick={handleResendEmail}
                  variant="secondary"
                  disabled={loading}
                >
                  재전송
                </Button>
                <Button onClick={() => setStep("verify")} disabled={loading}>
                  인증 코드 입력하기
                </Button>
              </div>
            )}
          </div>
        )}

        {step === "verify" && (
          <div className="space-y-4 text-gray-900">
            <Input
              label="인증 코드"
              value={authCode}
              onChange={(e) => setAuthCode(e.target.value)}
              required
              disabled={loading}
            />
            <div className="flex space-x-2">
              <Button
                onClick={handleVerifyEmail}
                disabled={loading || !authCode}
                className="flex-1"
              >
                인증하기
              </Button>
              <Button
                onClick={handleResendEmail}
                variant="secondary"
                disabled={loading}
              >
                재전송
              </Button>
            </div>
          </div>
        )}

        {step === "info" && (
          <form className="space-y-4 text-gray-900" onSubmit={handleSignup}>
            <Input
              label="닉네임"
              value={nickname}
              onChange={(e) => {
                setNickname(e.target.value);
                setNicknameAvailable(null);
                setNicknameMessage(null);
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
            {nicknameMessage && (
              <p
                className={`text-sm ${
                  nicknameAvailable === true ? "text-green-600" : "text-red-600"
                }`}
              >
                {nicknameMessage}
              </p>
            )}

            <Input
              label="비밀번호"
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                validatePassword(e.target.value);
              }}
              required
              disabled={loading}
            />

            {password && (
              <div className="text-sm space-y-1 mt-2">
                <p
                  className={
                    passwordValidation.minLength
                      ? "text-green-600"
                      : "text-red-500"
                  }
                >
                  {passwordValidation.minLength ? "✓" : "○"} 8자 이상
                </p>
                <p
                  className={
                    passwordValidation.hasEnglish
                      ? "text-green-600"
                      : "text-red-500"
                  }
                >
                  {passwordValidation.hasEnglish ? "✓" : "○"} 영문 포함
                </p>
                <p
                  className={
                    passwordValidation.hasNumber
                      ? "text-green-600"
                      : "text-red-500"
                  }
                >
                  {passwordValidation.hasNumber ? "✓" : "○"} 숫자 포함
                </p>
                <p
                  className={
                    passwordValidation.hasSpecialChar
                      ? "text-green-600"
                      : "text-red-500"
                  }
                >
                  {passwordValidation.hasSpecialChar ? "✓" : "○"} 특수문자 포함
                </p>
              </div>
            )}

            <Input
              label="비밀번호 확인"
              type="password"
              value={passwordConfirm}
              onChange={(e) => {
                setPasswordConfirm(e.target.value);
                if (e.target.value) {
                  setPasswordMatch(e.target.value === password);
                } else {
                  setPasswordMatch(null);
                }
              }}
              required
              disabled={loading}
            />

            {passwordConfirm && (
              <p
                className={`text-sm mt-2 ${
                  passwordMatch ? "text-green-600" : "text-red-600"
                }`}
              >
                {passwordMatch
                  ? "✓ 비밀번호가 일치합니다."
                  : "✗ 비밀번호가 일치하지 않습니다."}
              </p>
            )}

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "가입 중..." : "회원가입"}
            </Button>
          </form>
        )}

        {error && <ErrorState message={error} />}

        <div className="flex justify-center space-x-4 text-sm">
          <button
            type="button"
            onClick={() => router.push("/")}
            className="text-gray-600 hover:text-gray-800"
          >
            메인으로
          </button>
          <span className="text-gray-300">|</span>
          <button
            type="button"
            onClick={() => router.push("/login")}
            className="text-blue-600 hover:text-blue-800"
          >
            로그인으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
}

