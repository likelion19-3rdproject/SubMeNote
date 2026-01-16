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
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">정산 상세</h1>

      <Card className="mb-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">정산 정보</h2>
        <div className="space-y-2">
          <p>
            <span className="font-medium">정산 ID:</span> {settlement.id}
          </p>
          <p>
            <span className="font-medium">금액:</span> {settlement.amount.toLocaleString()}원
          </p>
          <p>
            <span className="font-medium">상태:</span> {settlement.status}
          </p>
          <p>
            <span className="font-medium">정산일:</span>{' '}
            {new Date(settlement.createdAt).toLocaleDateString()}
          </p>
        </div>
      </Card>

      <Card>
        <h2 className="text-xl font-semibold text-gray-900 mb-4">정산 항목</h2>
        {settlement.items && settlement.items.length > 0 ? (
          <div className="space-y-4">
            {settlement.items.map((item) => (
              <div key={item.id} className="border-b pb-4 last:border-b-0">
                <p>
                  <span className="font-medium">주문 ID:</span> {item.orderId}
                </p>
                <p>
                  <span className="font-medium">금액:</span> {item.amount.toLocaleString()}원
                </p>
                <p className="text-sm text-gray-500">
                  {new Date(item.createdAt).toLocaleDateString()}
                </p>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500">정산 항목이 없습니다.</p>
        )}
      </Card>
    </div>
  );
}

