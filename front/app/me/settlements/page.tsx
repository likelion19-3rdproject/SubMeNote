'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { settlementApi } from '@/src/api/settlementApi';
import { SettlementResponseDto } from '@/src/types/settlement';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';

export default function SettlementsPage() {
  const router = useRouter();
  const [settlements, setSettlements] = useState<Page<SettlementResponseDto> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [months, setMonths] = useState<number>(1); // 기본 1개월

  useEffect(() => {
    loadSettlements();
  }, [currentPage, months]);

  const loadSettlements = async () => {
    try {
      setLoading(true);
      setError(null);
      // TODO: 백엔드에 월별 조회 파라미터 추가 필요
      const data = await settlementApi.getSettlements(currentPage, 10);
      // 프론트에서 월별 필터링 (임시)
      const filteredData = filterByMonths(data, months);
      setSettlements(filteredData);
    } catch (err: any) {
      setError(err.response?.data?.message || '정산 내역을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 프론트에서 월별 필터링 (임시 - 백엔드 API에 월별 조회 기능 추가 필요)
  const filterByMonths = (data: Page<SettlementResponseDto>, months: number): Page<SettlementResponseDto> => {
    const now = new Date();
    const cutoffDate = new Date(now.getFullYear(), now.getMonth() - months, now.getDate());
    
    const filtered = data.content.filter((settlement) => {
      const settlementDate = new Date(settlement.createdAt);
      return settlementDate >= cutoffDate;
    });

    return {
      ...data,
      content: filtered,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / data.size),
    };
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
        <ErrorState message={error} onRetry={loadSettlements} />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">정산 내역</h1>
        <div className="flex items-center space-x-4">
          <label className="text-sm font-medium text-gray-700">조회 기간:</label>
          <select
            value={months}
            onChange={(e) => setMonths(Number(e.target.value))}
            className="px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((m) => (
              <option key={m} value={m}>
                {m}개월
              </option>
            ))}
          </select>
        </div>
      </div>

      {settlements && settlements.content.length > 0 ? (
        <>
          <div className="space-y-4 mb-6">
            {settlements.content.map((settlement) => (
              <Card
                key={settlement.id}
                onClick={() => router.push(`/me/settlements/${settlement.id}`)}
                className="hover:shadow-lg transition-shadow cursor-pointer"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">정산 #{settlement.id}</h3>
                    <p className="text-gray-600 mt-1">금액: {settlement.amount.toLocaleString()}원</p>
                    <p className="text-sm text-gray-500 mt-1">
                      정산일: {new Date(settlement.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm ${
                      settlement.status === 'COMPLETED'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {settlement.status}
                  </span>
                </div>
              </Card>
            ))}
          </div>
          <Pagination
            currentPage={currentPage}
            totalPages={settlements.totalPages}
            onPageChange={setCurrentPage}
          />
        </>
      ) : (
        <p className="text-gray-500">정산 내역이 없습니다.</p>
      )}
    </div>
  );
}

