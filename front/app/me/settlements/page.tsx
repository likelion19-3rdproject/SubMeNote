'use client';

import { useEffect, useState, useCallback } from 'react';
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

  // 프론트에서 월별 필터링 (임시 - 백엔드 API에 월별 조회 기능 추가 필요)
  const filterByMonths = useCallback((data: Page<SettlementResponseDto>, months: number): Page<SettlementResponseDto> => {
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
  }, []);

  const loadSettlements = useCallback(async () => {
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
  }, [currentPage, months, filterByMonths]);

  const loadPendingItems = useCallback(async () => {
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
  }, [currentPage]);

  useEffect(() => {
    if (activeTab === 'completed') {
      loadSettlements();
    } else {
      loadPendingItems();
    }
  }, [currentPage, months, activeTab, loadSettlements, loadPendingItems]);

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
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in-scale">
      <div className="flex justify-between items-center mb-10">
        <h1 className="text-4xl font-black text-white"><span>💰</span> <span className="gradient-text">정산 내역</span></h1>
        <div className="flex items-center space-x-4">
          <label className="text-sm font-bold text-gray-300">조회 기간:</label>
          <select
            value={months}
            onChange={(e) => setMonths(Number(e.target.value))}
            className="px-4 py-2 glass border-2 border-purple-500/30 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500/60 text-white transition-all duration-300"
          >
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((m) => (
              <option key={m} value={m} className="bg-gray-800 text-white">
                {m}개월
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* 탭 메뉴 */}
      <div className="flex space-x-4 mb-8 border-b border-purple-500/30">
        <button
          onClick={() => {
            setActiveTab('completed');
            setCurrentPage(0);
          }}
          className={`px-6 py-3 font-bold text-sm transition-all duration-300 relative overflow-hidden group ${
            activeTab === 'completed'
              ? 'text-white border-b-2 border-purple-500'
              : 'text-gray-500 hover:text-gray-300'
          }`}
        >
          <span className="relative z-10">완료된 정산</span>
          {activeTab !== 'completed' && (
            <div className="absolute inset-0 bg-gradient-to-r from-purple-600/0 via-purple-600/20 to-purple-600/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700"></div>
          )}
        </button>
        <button
          onClick={() => {
            setActiveTab('pending');
            setCurrentPage(0);
          }}
          className={`px-6 py-3 font-bold text-sm transition-all duration-300 relative overflow-hidden group ${
            activeTab === 'pending'
              ? 'text-white border-b-2 border-pink-500'
              : 'text-gray-500 hover:text-gray-300'
          }`}
        >
          <span className="relative z-10">대기 중인 정산</span>
          {activeTab !== 'pending' && (
            <div className="absolute inset-0 bg-gradient-to-r from-pink-600/0 via-pink-600/20 to-pink-600/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700"></div>
          )}
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
                      <h3 className="text-xl font-black text-white mb-2">
                        정산 #{settlement.id}
                      </h3>
                      <p className="text-gray-300 mt-1 font-medium">
                        크리에이터: {settlement.creatorNickname}
                      </p>
                      <p className="text-purple-400 mt-1 font-bold text-lg">
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
                      className={`px-4 py-2 rounded-full text-sm font-bold ${
                        settlement.status === 'COMPLETED'
                          ? 'bg-gradient-to-r from-green-600 to-emerald-600 text-white neon-glow'
                          : settlement.status === 'PENDING'
                          ? 'bg-gradient-to-r from-yellow-600 to-orange-600 text-white'
                          : 'bg-gradient-to-r from-red-600 to-pink-600 text-white'
                      }`}
                    >
                      {settlement.status === 'COMPLETED' ? '✓ 완료' : 
                       settlement.status === 'PENDING' ? '⏳ 대기' : '✗ 실패'}
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
          <div className="glass p-12 text-center rounded-2xl border border-purple-500/20 animate-fade-in-scale">
            <div className="text-7xl mb-6 animate-pulse">📭</div>
            <p className="text-gray-400 text-xl font-bold">완료된 정산 내역이 없습니다.</p>
          </div>
        )
      ) : (
      // 대기 중인 정산 (SettlementItem)
      pendingItems && pendingItems.content.length > 0 ? (
        <>
          <div className="mb-6 flex justify-end">
            <button
              onClick={handleSettleImmediately}
              disabled={settling}
              className={`btn-interactive px-8 py-3 rounded-xl font-bold transition-all duration-300 ${
                settling
                  ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                  : 'bg-gradient-to-r from-blue-600 to-cyan-600 text-white hover:from-blue-500 hover:to-cyan-500 neon-glow hover:scale-105'
              }`}
            >
              <span className="relative z-10">{settling ? '⏳ 정산 처리 중...' : '⚡ 즉시 정산 처리'}</span>
            </button>
          </div>
          <div className="space-y-4 mb-6">
              {pendingItems.content.map((item, index) => (
                <Card key={item.id} className="animate-fade-in-scale" style={{animationDelay: `${index * 0.1}s`}}>
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xl font-black text-white mb-2">
                        정산 항목 #{item.id}
                      </h3>
                      <p className="text-gray-300 mt-1 font-medium">
                        결제 ID: {item.paymentId}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">
                        결제일: {new Date(item.createdAt).toLocaleDateString()}
                      </p>
                      <div className="mt-4 space-y-2 glass p-4 rounded-xl border border-purple-500/20">
                        <p className="text-sm text-gray-300">
                          <span className="font-bold">결제 금액:</span>{' '}
                          <span className="text-white font-black">{item.totalAmount.toLocaleString()}원</span>
                        </p>
                        <p className="text-sm text-gray-400">
                          <span className="font-bold">플랫폼 수수료 (10%):</span>{' '}
                          <span className="text-red-400 font-bold">-{item.platformFee.toLocaleString()}원</span>
                        </p>
                        <p className="text-base font-black text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400">
                          <span className="text-gray-300 font-bold">정산 금액 (90%):</span>{' '}
                          {item.settlementAmount.toLocaleString()}원
                        </p>
                      </div>
                    </div>
                    <span
                      className={`px-4 py-2 rounded-full text-sm font-bold ${
                        item.status === 'CONFIRMED'
                          ? 'bg-gradient-to-r from-green-600 to-emerald-600 text-white neon-glow'
                          : 'bg-gradient-to-r from-yellow-600 to-orange-600 text-white'
                      }`}
                    >
                      {item.status === 'CONFIRMED' ? '✓ 확정' : '📝 기록됨'}
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
          <div className="glass p-12 text-center rounded-2xl border border-purple-500/20 animate-fade-in-scale">
            <div className="text-7xl mb-6 animate-pulse">⏳</div>
            <p className="text-gray-400 text-xl font-bold">대기 중인 정산 내역이 없습니다.</p>
          </div>
        )
      )}
    </div>
  );
}

