'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { settlementApi } from '@/src/api/settlementApi';
import { SettlementResponseDto, SettlementItemResponse } from '@/src/types/settlement';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';

type TabType = 'completed' | 'pending';

export default function SettlementsPage() {
  const router = useRouter();
  const [settlements, setSettlements] = useState<Page<SettlementResponseDto> | null>(null);
  const [pendingItems, setPendingItems] = useState<Page<SettlementItemResponse> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [months, setMonths] = useState<number>(1); // 기본 1개월
  const [activeTab, setActiveTab] = useState<TabType>('completed'); // 완료된 정산 / 대기 중인 정산
  const [settling, setSettling] = useState(false); // 즉시 정산 처리 중 상태

  useEffect(() => {
    if (activeTab === 'completed') {
      loadSettlements();
    } else {
      loadPendingItems();
    }
  }, [currentPage, months, activeTab]);

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

  const loadPendingItems = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await settlementApi.getPendingSettlementItems(currentPage, 10);
      setPendingItems(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '대기 중인 정산 내역을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 프론트에서 월별 필터링 (임시 - 백엔드 API에 월별 조회 기능 추가 필요)
  const filterByMonths = (data: Page<SettlementResponseDto>, months: number): Page<SettlementResponseDto> => {
    const now = new Date();
    const cutoffDate = new Date(now.getFullYear(), now.getMonth() - months, now.getDate());
    
    const filtered = data.content.filter((settlement) => {
      const settlementDate = settlement.settledAt ? new Date(settlement.settledAt) : new Date(settlement.periodEnd);
      return settlementDate >= cutoffDate;
    });

    return {
      ...data,
      content: filtered,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / data.size),
    };
  };

  // 즉시 정산 처리
  const handleSettleImmediately = async () => {
    if (!confirm('대기 중인 모든 정산 항목을 즉시 정산 처리하시겠습니까?')) {
      return;
    }

    try {
      setSettling(true);
      setError(null);
      const settlement = await settlementApi.settleImmediately();
      
      // 성공 메시지
      alert(`정산이 완료되었습니다.\n정산 ID: #${settlement.id}\n정산 금액: ${settlement.totalAmount.toLocaleString()}원`);
      
      // 대기 중인 정산 목록 새로고침
      await loadPendingItems();
      
      // 완료된 정산 탭으로 전환하여 새로 생성된 정산 확인 가능
      setActiveTab('completed');
      setCurrentPage(0);
      await loadSettlements();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || '정산 처리에 실패했습니다.';
      setError(errorMessage);
      alert(errorMessage);
    } finally {
      setSettling(false);
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

      {/* 탭 메뉴 */}
      <div className="flex space-x-4 mb-6 border-b border-gray-200">
        <button
          onClick={() => {
            setActiveTab('completed');
            setCurrentPage(0);
          }}
          className={`px-6 py-3 font-medium text-sm transition-colors ${
            activeTab === 'completed'
              ? 'text-blue-600 border-b-2 border-blue-600'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          완료된 정산
        </button>
        <button
          onClick={() => {
            setActiveTab('pending');
            setCurrentPage(0);
          }}
          className={`px-6 py-3 font-medium text-sm transition-colors ${
            activeTab === 'pending'
              ? 'text-blue-600 border-b-2 border-blue-600'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          대기 중인 정산
        </button>
      </div>

      {activeTab === 'completed' ? (
        // 완료된 정산 (Settlement)
        settlements && settlements.content.length > 0 ? (
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
                      <h3 className="text-lg font-semibold text-gray-900">
                        정산 #{settlement.id}
                      </h3>
                      <p className="text-gray-600 mt-1">
                        크리에이터: {settlement.creatorNickname}
                      </p>
                      <p className="text-gray-600 mt-1">
                        금액: {settlement.totalAmount.toLocaleString()}원
                      </p>
                      <p className="text-sm text-gray-500 mt-1">
                        정산 기간: {settlement.periodStart} ~ {settlement.periodEnd}
                      </p>
                      {settlement.settledAt && (
                        <p className="text-sm text-gray-500 mt-1">
                          정산일: {new Date(settlement.settledAt).toLocaleDateString()}
                        </p>
                      )}
                    </div>
                    <span
                      className={`px-3 py-1 rounded-full text-sm ${
                        settlement.status === 'COMPLETED'
                          ? 'bg-green-100 text-green-800'
                          : settlement.status === 'PENDING'
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-red-100 text-red-800'
                      }`}
                    >
                      {settlement.status === 'COMPLETED' ? '완료' : 
                       settlement.status === 'PENDING' ? '대기' : '실패'}
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
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">완료된 정산 내역이 없습니다.</p>
          </div>
        )
      ) : (
      // 대기 중인 정산 (SettlementItem)
      pendingItems && pendingItems.content.length > 0 ? (
        <>
          <div className="mb-4 flex justify-end">
            <button
              onClick={handleSettleImmediately}
              disabled={settling}
              className={`px-6 py-2 rounded-md font-medium transition-colors ${
                settling
                  ? 'bg-gray-400 text-gray-600 cursor-not-allowed'
                  : 'bg-blue-600 text-white hover:bg-blue-700'
              }`}
            >
              {settling ? '정산 처리 중...' : '즉시 정산 처리'}
            </button>
          </div>
          <div className="space-y-4 mb-6">
              {pendingItems.content.map((item) => (
                <Card key={item.id} className="hover:shadow-lg transition-shadow">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900">
                        정산 항목 #{item.id}
                      </h3>
                      <p className="text-gray-600 mt-1">
                        결제 ID: {item.paymentId}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">
                        결제일: {new Date(item.createdAt).toLocaleDateString()}
                      </p>
                      <div className="mt-3 space-y-1">
                        <p className="text-sm">
                          <span className="font-medium">결제 금액:</span>{' '}
                          {item.totalAmount.toLocaleString()}원
                        </p>
                        <p className="text-sm text-gray-600">
                          <span className="font-medium">플랫폼 수수료 (10%):</span>{' '}
                          {item.platformFee.toLocaleString()}원
                        </p>
                        <p className="text-sm font-semibold text-blue-600">
                          <span className="font-medium">정산 금액 (90%):</span>{' '}
                          {item.settlementAmount.toLocaleString()}원
                        </p>
                      </div>
                    </div>
                    <span
                      className={`px-3 py-1 rounded-full text-sm ${
                        item.status === 'CONFIRMED'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-yellow-100 text-yellow-800'
                      }`}
                    >
                      {item.status === 'CONFIRMED' ? '확정' : '기록됨'}
                    </span>
                  </div>
                </Card>
              ))}
            </div>
            <Pagination
              currentPage={currentPage}
              totalPages={pendingItems.totalPages}
              onPageChange={setCurrentPage}
            />
          </>
        ) : (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">대기 중인 정산 내역이 없습니다.</p>
          </div>
        )
      )}
    </div>
  );
}

