'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { subscribeApi } from '@/src/api/subscribeApi';
import { SubscribedCreatorResponseDto } from '@/src/types/subscribe';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';

export default function SubscriptionsPage() {
  const router = useRouter();
  const [creators, setCreators] = useState<Page<SubscribedCreatorResponseDto> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadCreators();
  }, [currentPage]);

  const loadCreators = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await subscribeApi.getMyCreators(currentPage, 10);
      setCreators(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '구독 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <ErrorState message={error} onRetry={loadCreators} />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">내가 구독한 크리에이터</h1>

      {creators && creators.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
            {creators.content.map((creator) => (
              <Card
                key={creator.creatorId}
                onClick={() => router.push(`/creators/${creator.creatorId}`)}
                className="hover:shadow-lg transition-shadow cursor-pointer"
              >
                <h3 className="text-xl font-semibold text-gray-900">{creator.creatorNickname}</h3>
                <p className="text-sm text-gray-500 mt-2">
                  {creator.type === 'PAID' ? '멤버십' : '일반 구독'} - {creator.status === 'ACTIVE' ? '활성' : '취소됨'}
                </p>
                {creator.expiredAt && (
                  <p className="text-xs text-gray-400 mt-1">
                    만료일: {new Date(creator.expiredAt).toLocaleDateString()}
                  </p>
                )}
              </Card>
            ))}
          </div>
          <Pagination
            currentPage={currentPage}
            totalPages={creators.totalPages}
            onPageChange={setCurrentPage}
          />
        </>
      ) : (
        <p className="text-gray-500">구독한 크리에이터가 없습니다.</p>
      )}
    </div>
  );
}

