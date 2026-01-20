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
      <div className="max-w-7xl mx-auto px-6 py-16">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-6 py-16">
        <ErrorState message={error} onRetry={loadCreators} />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-6 py-16">
      <h1 className="text-3xl font-bold text-gray-900 mb-12">내가 구독한 크리에이터</h1>

      {creators && creators.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {creators.content.map((creator) => (
              <Card
                key={creator.creatorId}
                onClick={() => router.push(`/creators/${creator.creatorId}`)}
                className="hover:shadow-lg transition-shadow cursor-pointer"
              >
                <h3 className="text-xl font-semibold text-gray-900 mb-2">{creator.creatorNickname}</h3>
                <p className="text-sm text-gray-500 mb-1">
                  {creator.type === 'PAID' ? '멤버십' : '일반 구독'} - {creator.status === 'ACTIVE' ? '활성' : '취소됨'}
                </p>
                {creator.expiredAt && (
                  <p className="text-xs text-gray-400">
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
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-16 text-center">
          <div className="mb-6">
            <svg
              className="w-20 h-20 mx-auto text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
              />
            </svg>
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-3">
            아직 구독한 크리에이터가 없습니다
          </h3>
          <p className="text-gray-500 max-w-md mx-auto">
            관심 있는 크리에이터를 찾아 구독하면<br />
            멤버십 전용 콘텐츠를 만나볼 수 있습니다
          </p>
          {/* TODO: 크리에이터 탐색 기능 구현 시 아래 버튼 활성화 */}
          {/* <button
            onClick={() => router.push('/creators')}
            className="bg-[#FFC837] hover:bg-[#FFB800] text-gray-900 px-8 py-3 text-base font-semibold rounded-xl transition-all duration-200 shadow-sm hover:shadow mt-8"
          >
            크리에이터 둘러보기
          </button> */}
        </div>
      )}
    </div>
  );
}

