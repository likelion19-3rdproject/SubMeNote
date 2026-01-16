"use client";

import { useEffect, useState } from "react";
import { userApi } from "@/src/api/userApi";
import Input from "@/src/components/common/Input";
import Button from "@/src/components/common/Button";
import LoadingSpinner from "@/src/components/common/LoadingSpinner";
import ErrorState from "@/src/components/common/ErrorState";

export default function AccountPage() {
  const [bankName, setBankName] = useState("");
  const [accountNumber, setAccountNumber] = useState("");
  const [holderName, setHolderName] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [hasAccount, setHasAccount] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  // 기존 계좌 정보 로드
  useEffect(() => {
    const loadAccount = async () => {
      try {
        setLoading(true);
        const account = await userApi.getAccount();
        setBankName(account.bankName);
        setAccountNumber(account.accountNumber);
        setHolderName(account.holderName);
        setHasAccount(true);
      } catch (err: any) {
        // 404 에러면 계좌가 없는 것이므로 신규 등록 모드
        if (err.response?.status === 404) {
          setHasAccount(false);
          setIsEditing(true); // 등록 모드로 시작
        } else {
          setError(
            err.response?.data?.message ||
              "계좌 정보를 불러오는데 실패했습니다."
          );
        }
      } finally {
        setLoading(false);
      }
    };
    loadAccount();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setSubmitting(true);

    try {
      if (hasAccount) {
        // 기존 계좌 수정
        await userApi.updateAccount({ bankName, accountNumber, holderName });
        setSuccess(true);
        setIsEditing(false);
      } else {
        // 신규 계좌 등록
        await userApi.createAccount({ bankName, accountNumber, holderName });
        setSuccess(true);
        setHasAccount(true);
        setIsEditing(false);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || "계좌 정보 저장에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    // 원래 값으로 복원하려면 다시 로드
    setIsEditing(false);
    setError(null);
    setSuccess(false);
    // 필요시 loadAccount 재호출
  };

  if (loading) {
    return (
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">계좌 관리</h1>
        {hasAccount && !isEditing && (
          <Button onClick={() => setIsEditing(true)} variant="secondary">
            수정하기
          </Button>
        )}
      </div>

      {error && <ErrorState message={error} />}
      {success && (
        <div className="mb-4 p-4 bg-green-100 text-green-800 rounded-md">
          계좌 정보가 저장되었습니다.
        </div>
      )}

      {hasAccount && !isEditing ? (
        // 계좌 정보 표시 모드
        <div className="space-y-6 bg-white p-6 rounded-lg border border-gray-200">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              은행명
            </label>
            <p className="text-gray-900 text-lg">{bankName}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              계좌번호
            </label>
            <p className="text-gray-900 text-lg">{accountNumber}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              예금주명
            </label>
            <p className="text-gray-900 text-lg">{holderName}</p>
          </div>
        </div>
      ) : (
        // 입력 폼 모드 (신규 등록 또는 수정)
        <form onSubmit={handleSubmit} className="text-gray-900 space-y-4">
          <Input
            label="은행명"
            value={bankName}
            onChange={(e) => setBankName(e.target.value)}
            required
            disabled={submitting}
          />
          <Input
            label="계좌번호"
            value={accountNumber}
            onChange={(e) => setAccountNumber(e.target.value)}
            required
            disabled={submitting}
          />
          <Input
            label="예금주명"
            value={holderName}
            onChange={(e) => setHolderName(e.target.value)}
            required
            disabled={submitting}
          />
          <div className="flex space-x-4">
            <Button type="submit" disabled={submitting} className="flex-1">
              {submitting ? "저장 중..." : hasAccount ? "수정하기" : "등록하기"}
            </Button>
            {hasAccount && (
              <Button
                type="button"
                onClick={handleCancel}
                variant="secondary"
                disabled={submitting}
                className="flex-1"
              >
                취소
              </Button>
            )}
          </div>
        </form>
      )}
    </div>
  );
}
