'use client';

import { useEffect, useState } from 'react';
import { applicationApi } from '@/src/api/applicationApi';
import { CreatorApplicationResponse } from '@/src/types/application';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';

export default function CreatorApplicationPage() {
  const [application, setApplication] = useState<CreatorApplicationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [applying, setApplying] = useState(false);

  const loadApplication = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await applicationApi.getMyApplication();
      setApplication(data);
    } catch (err: any) {
      // 404는 신청 내역이 없는 것
      if (err.response?.status === 404) {
        setApplication(null);
      } else {
        setError(err.response?.data?.message || '신청 내역을 불러오는데 실패했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadApplication();
  }, []);

  const handleApply = async () => {
    if (!confirm('크리에이터로 신청하시겠습니까?')) return;

    try {
      setApplying(true);
      await applicationApi.applyForCreator();
      alert('크리에이터 신청이 완료되었습니다. 관리자의 승인을 기다려주세요.');
      loadApplication();
    } catch (err: any) {
      alert(err.response?.data?.message || '신청에 실패했습니다.');
    } finally {
      setApplying(false);
    }
  };

  const getStatusDisplay = (status: string) => {
    switch (status) {
      case 'PENDING':
        return { text: '승인 대기 중', color: 'bg-yellow-100 text-yellow-800' };
      case 'APPROVED':
        return { text: '승인됨', color: 'bg-green-100 text-green-800' };
      case 'REJECTED':
        return { text: '거절됨', color: 'bg-red-100 text-red-800' };
      default:
        return { text: status, color: 'bg-gray-100 text-gray-800' };
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-12">
        <ErrorState message={error} onRetry={loadApplication} />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">크리에이터 신청</h1>

      {application ? (
        <Card>
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">신청 상태</h3>
              <span
                className={`inline-block px-4 py-2 rounded-full text-sm font-medium ${
                  getStatusDisplay(application.status).color
                }`}
              >
                {getStatusDisplay(application.status).text}
              </span>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">신청자</h3>
              <p className="text-gray-600">{application.nickname}</p>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">신청일</h3>
              <p className="text-gray-600">
                {new Date(application.appliedAt).toLocaleDateString('ko-KR', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </p>
            </div>

            {application.status === 'REJECTED' && (
              <div className="mt-6">
                <Button onClick={handleApply} disabled={applying} className="w-full">
                  {applying ? '신청 중...' : '다시 신청하기'}
                </Button>
              </div>
            )}
          </div>
        </Card>
      ) : (
        <Card>
          <div className="text-center py-8">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              크리에이터 신청 내역이 없습니다
            </h3>
            <p className="text-gray-600 mb-6">
              크리에이터가 되어 게시글을 작성하고 수익을 창출하세요.
            </p>
            <Button onClick={handleApply} disabled={applying}>
              {applying ? '신청 중...' : '크리에이터 신청하기'}
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
}
