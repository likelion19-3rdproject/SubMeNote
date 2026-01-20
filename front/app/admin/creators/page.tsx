'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { adminApi } from '@/src/api/adminApi';
import { CreatorResponseDto } from '@/src/types/user';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';
import CreatorProfileImage from '@/src/components/common/CreatorProfileImage';

export default function AdminCreatorsPage() {
  const [creators, setCreators] = useState<Page<CreatorResponseDto> | null>(null);
  const [creatorCount, setCreatorCount] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 크리에이터 수와 목록을 동시에 가져옴
      const [countData, listData] = await Promise.all([
        adminApi.getCreatorCount(),
        adminApi.getCreatorList(currentPage, 20),
      ]);
      
      setCreatorCount(countData);
      setCreators(listData);
    } catch (err: any) {
      setError(err.response?.data?.message || '데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadData();
  }, [loadData]);

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
        <ErrorState message={error} onRetry={loadData} />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-12 animate-fade-in-scale">
      <div className="mb-8">
        <h1 className="text-4xl font-black text-white mb-10"><span>🎨</span> <span className="gradient-text">크리에이터 관리</span></h1>
        <div className="glass border border-purple-400/20 rounded-lg p-4">
          <p className="text-lg text-gray-200">
            전체 크리에이터 수: <span className="font-bold text-purple-400">{creatorCount}명</span>
          </p>
        </div>
      </div>

      {creators && creators.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {creators.content.map((creator) => (
              <Link key={creator.creatorId} href={`/creators/${creator.creatorId}`}>
                <Card>
                  <div className="text-center py-4 cursor-pointer hover:bg-white/5 transition-colors rounded-lg">
                    <div className="flex justify-center mb-3">
                      <CreatorProfileImage 
                        creatorId={creator.creatorId} 
                        nickname={creator.nickname}
                        size="sm"
                      />
                    </div>
                    <h3 className="text-lg font-semibold text-white">
                      {creator.nickname}
                    </h3>
                  </div>
                </Card>
              </Link>
            ))}
          </div>

          {creators.totalPages > 1 && (
            <div className="mt-8">
              <Pagination
                currentPage={currentPage}
                totalPages={creators.totalPages}
                onPageChange={setCurrentPage}
              />
            </div>
          )}
        </>
      ) : (
        <Card>
          <div className="text-center py-8">
            <p className="text-gray-400">등록된 크리에이터가 없습니다.</p>
          </div>
        </Card>
      )}
    </div>
  );
}
