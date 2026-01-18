'use client';

import { useEffect, useState, useCallback } from 'react';
import { adminApi } from '@/src/api/adminApi';
import { CreatorApplicationResponse } from '@/src/types/application';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';

export default function CreatorApplicationsPage() {
  const [applications, setApplications] = useState<Page<CreatorApplicationResponse> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processing, setProcessing] = useState<number | null>(null);

  const loadApplications = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await adminApi.getCreatorApplications(currentPage, 10);
      setApplications(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '신청 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadApplications();
  }, [loadApplications]);

  const handleProcess = async (applicationId: number, approved: boolean) => {
    const message = approved ? '승인하시겠습니까?' : '거절하시겠습니까?';
    if (!confirm(message)) return;

    try {
      setProcessing(applicationId);
      await adminApi.processApplication(applicationId, { approved });
      alert(approved ? '승인되었습니다.' : '거절되었습니다.');
      loadApplications();
    } catch (err: any) {
      alert(err.response?.data?.message || '처리에 실패했습니다.');
    } finally {
      setProcessing(null);
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
      <div className="max-w-6xl mx-auto px-6 py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-12">
        <ErrorState message={error} onRetry={loadApplications} />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">크리에이터 신청 관리</h1>

      {applications && applications.content.length > 0 ? (
        <>
          <div className="space-y-4">
            {applications.content.map((app) => (
              <Card key={app.id}>
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">
                      {app.nickname}
                    </h3>
                    <p className="text-sm text-gray-500">
                      신청일: {new Date(app.appliedAt).toLocaleDateString('ko-KR', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </p>
                  </div>

                  <div className="flex items-center gap-4">
                    <span
                      className={`inline-block px-4 py-2 rounded-full text-sm font-medium ${
                        getStatusDisplay(app.status).color
                      }`}
                    >
                      {getStatusDisplay(app.status).text}
                    </span>

                    {app.status === 'PENDING' && (
                      <div className="flex gap-2">
                        <Button
                          onClick={() => handleProcess(app.id, true)}
                          disabled={processing === app.id}
                          size="sm"
                        >
                          승인
                        </Button>
                        <Button
                          onClick={() => handleProcess(app.id, false)}
                          variant="danger"
                          disabled={processing === app.id}
                          size="sm"
                        >
                          거절
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {applications.totalPages > 1 && (
            <div className="mt-8">
              <Pagination
                currentPage={currentPage}
                totalPages={applications.totalPages}
                onPageChange={setCurrentPage}
              />
            </div>
          )}
        </>
      ) : (
        <Card>
          <div className="text-center py-8">
            <p className="text-gray-500">대기 중인 신청이 없습니다.</p>
          </div>
        </Card>
      )}
    </div>
  );
}
