'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { settlementApi } from '@/src/api/settlementApi';
import { SettlementDetailResponse } from '@/src/types/settlement';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';

export default function SettlementDetailPage() {
  const params = useParams();
  const settlementId = Number(params.settlementId);
  const [settlement, setSettlement] = useState<SettlementDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (settlementId) {
      loadSettlement();
    }
  }, [settlementId]);

  const loadSettlement = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await settlementApi.getSettlement(settlementId);
      setSettlement(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '정산 내역을 불러오는데 실패했습니다.');
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
        <ErrorState message={error} onRetry={loadSettlement} />
      </div>
    );
  }

  if (!settlement) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <p className="text-gray-500">정산 내역을 찾을 수 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in-scale">
      <h1 className="text-4xl font-black text-white mb-10"><span>💰</span> <span className="gradient-text">정산 상세</span></h1>

      <Card className="mb-6">
        <h2 className="text-xl font-semibold text-white mb-4">정산 정보</h2>
        <div className="space-y-2">
          <p className="text-gray-200">
            <span className="font-medium">정산 ID:</span> <span className="text-white">{settlement.settlementId}</span>
          </p>
          <p className="text-gray-200">
            <span className="font-medium">정산 기간:</span> <span className="text-white">{settlement.periodStart} ~ {settlement.periodEnd}</span>
          </p>
          <p className="text-gray-200">
            <span className="font-medium">총 금액:</span> <span className="text-white font-bold">{settlement.totalAmount.toLocaleString()}원</span>
          </p>
          <p className="text-gray-200">
            <span className="font-medium">상태:</span>{' '}
            <span
              className={`px-3 py-1 rounded-full text-sm font-bold ${
                settlement.status === 'COMPLETED'
                  ? 'bg-green-500/20 text-green-400 border border-green-500/30'
                  : settlement.status === 'PENDING'
                  ? 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30'
                  : 'bg-red-500/20 text-red-400 border border-red-500/30'
              }`}
            >
              {settlement.status === 'COMPLETED' ? '✓ 완료' : 
               settlement.status === 'PENDING' ? '⏳ 대기' : '✗ 실패'}
            </span>
          </p>
          {settlement.settledAt && (
            <p className="text-gray-200">
              <span className="font-medium">정산일:</span>{' '}
              <span className="text-white">{new Date(settlement.settledAt).toLocaleDateString()}</span>
            </p>
          )}
        </div>
      </Card>

      <Card>
        <h2 className="text-xl font-semibold text-white mb-4">정산 항목</h2>
        {settlement.items && settlement.items.content && settlement.items.content.length > 0 ? (
          <div className="space-y-4">
            {settlement.items.content.map((item) => (
              <div key={item.id} className="relative pb-4 last:pb-0 mb-4 last:mb-0">
                {/* 구분선 */}
                {index < settlement.items.content.length - 1 && (
                  <>
                    <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-purple-400/40 to-transparent"></div>
                    <div className="absolute bottom-0 left-0 right-0 h-[1px] bg-purple-400/20 blur-sm"></div>
                  </>
                )}
                <div className="flex justify-between items-start">
                  <div>
                    <p className="text-gray-200">
                      <span className="font-medium">결제 ID:</span> <span className="text-white">{item.paymentId}</span>
                    </p>
                    <p className="text-sm text-gray-400 mt-1">
                      {new Date(item.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm font-bold ${
                      item.status === 'CONFIRMED'
                        ? 'bg-green-500/20 text-green-400 border border-green-500/30'
                        : 'bg-gray-500/20 text-gray-300 border border-gray-500/30'
                    }`}
                  >
                    {item.status === 'CONFIRMED' ? '✓ 확정' : '📝 기록됨'}
                  </span>
                </div>
                <div className="mt-2 space-y-1">
                  <p className="text-sm text-gray-200">
                    <span className="font-medium">결제 금액:</span>{' '}
                    <span className="text-white font-bold">{item.totalAmount.toLocaleString()}원</span>
                  </p>
                  <p className="text-sm text-gray-300">
                    <span className="font-medium">플랫폼 수수료 (10%):</span>{' '}
                    <span className="text-red-400 font-bold">-{item.platformFee.toLocaleString()}원</span>
                  </p>
                  <p className="text-base font-black gradient-text">
                    <span className="text-gray-200 font-medium">정산 금액 (90%):</span>{' '}
                    {item.settlementAmount.toLocaleString()}원
                  </p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="glass p-12 text-center rounded-2xl border border-purple-400/20 animate-fade-in-scale">
            <div className="text-7xl mb-6 animate-pulse">📭</div>
            <p className="text-gray-400 text-xl font-bold">정산 항목이 없습니다.</p>
          </div>
        )}
        {settlement.items && settlement.items.totalPages > 1 && (
          <div className="mt-4 text-sm text-gray-500 text-center">
            페이지 {settlement.items.number + 1} / {settlement.items.totalPages} (총 {settlement.items.totalElements}개)
          </div>
        )}
      </Card>
    </div>
  );
}

