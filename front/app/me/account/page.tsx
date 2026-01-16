'use client';

import { useEffect, useState } from 'react';
import { userApi } from '@/src/api/userApi';
import Input from '@/src/components/common/Input';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';

export default function AccountPage() {
  const [bankName, setBankName] = useState('');
  const [accountNumber, setAccountNumber] = useState('');
  const [holderName, setHolderName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // TODO: 기존 계좌 정보 로드 API 필요
  // useEffect(() => {
  //   const loadAccount = async () => {
  //     try {
  //       const account = await userApi.getAccount();
  //       setBankName(account.bankName);
  //       setAccountNumber(account.accountNumber);
  //       setHolderName(account.holderName);
  //     } catch (error) {
  //       console.error('Failed to load account:', error);
  //     }
  //   };
  //   loadAccount();
  // }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);

    try {
      // TODO: 기존 계좌가 있는지 확인하여 create/update 분기
      // 현재는 항상 create로 처리
      await userApi.createAccount({ bankName, accountNumber, holderName });
      setSuccess(true);
    } catch (err: any) {
      setError(err.response?.data?.message || '계좌 등록에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);

    try {
      await userApi.updateAccount({ bankName, accountNumber, holderName });
      setSuccess(true);
    } catch (err: any) {
      setError(err.response?.data?.message || '계좌 수정에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">계좌 관리</h1>

      {error && <ErrorState message={error} />}
      {success && (
        <div className="mb-4 p-4 bg-green-100 text-green-800 rounded-md">
          계좌 정보가 저장되었습니다.
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="은행명"
          value={bankName}
          onChange={(e) => setBankName(e.target.value)}
          required
          disabled={loading}
        />
        <Input
          label="계좌번호"
          value={accountNumber}
          onChange={(e) => setAccountNumber(e.target.value)}
          required
          disabled={loading}
        />
        <Input
          label="예금주명"
          value={holderName}
          onChange={(e) => setHolderName(e.target.value)}
          required
          disabled={loading}
        />
        <div className="flex space-x-4">
          <Button type="submit" disabled={loading} className="flex-1">
            {loading ? '저장 중...' : '등록'}
          </Button>
          <Button type="button" onClick={handleUpdate} variant="secondary" disabled={loading} className="flex-1">
            {loading ? '수정 중...' : '수정'}
          </Button>
        </div>
      </form>
    </div>
  );
}

