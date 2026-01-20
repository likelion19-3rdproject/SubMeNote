"use client";

import { useEffect, useState, useCallback } from "react";
import { orderApi } from "@/src/api/orderApi";
import { OrderResponseDto } from "@/src/types/order";
import { Page } from "@/src/types/common";
import Card from "@/src/components/common/Card";
import LoadingSpinner from "@/src/components/common/LoadingSpinner";
import ErrorState from "@/src/components/common/ErrorState";
import Pagination from "@/src/components/common/Pagination";

export default function OrdersPage() {
  const [orders, setOrders] = useState<Page<OrderResponseDto> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // [수정 1] loadOrders를 useEffect 밖으로 꺼내고 useCallback으로 감쌈
  // 이렇게 해야 아래쪽 return 문(JSX)에 있는 'onRetry' 버튼에서 이 함수를 쓸 수 있습니다.
  const loadOrders = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await orderApi.getOrders(currentPage, 10);
      setOrders(data);
    } catch (err: any) {
      setError(
        err.response?.data?.message || "주문 내역을 불러오는데 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  }, [currentPage]); // currentPage가 바뀔 때만 함수 재생성

  // [수정 2] useEffect에서는 만들어둔 loadOrders 호출만 함
  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  const getStatusDisplay = (status: string) => {
    switch (status) {
      case "PAID":
        return "완료";
      case "CANCELED":
        return "취소";
      case "PENDING":
        return "대기중";
      case "IN_PROGRESS":
        return "진행중";
      case "FAILED":
        return "실패";
      case "EXPIRED":
        return "만료";
      default:
        return status;
    }
  };

  // [수정 3] ?. 안전장치 추가 (orders?.content?.filter)
  // 데이터가 없거나 로딩 중일 때 filter 함수가 없다는 에러를 방지합니다.
  const filteredOrders = orders?.content?.filter((order) => {
    if (statusFilter === "ALL") return true;
    if (statusFilter === "COMPLETED") return order.status === "PAID";
    if (statusFilter === "CANCELLED") return order.status === "CANCELED";
    if (statusFilter === "FAILED") return order.status === "FAILED"; // 실패 필터 추가
    return order.status === statusFilter;
  });

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-16">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-16">
        <ErrorState message={error} onRetry={loadOrders} />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-16">
      <h1 className="text-3xl font-bold text-gray-900 mb-12">주문 내역</h1>

      {/* 상태 필터 */}
      <div className="mb-8">
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#FFC837] focus:border-[#FFC837] transition-all duration-200 text-sm font-medium text-gray-700"
        >
          <option value="ALL">전체</option>
          <option value="COMPLETED">완료</option>
          <option value="CANCELLED">취소</option>
          <option value="PENDING">대기중</option>
          <option value="FAILED">실패</option>
        </select>
      </div>

      {filteredOrders && filteredOrders.length > 0 ? (
        <>
          <div className="grid gap-4">
            {filteredOrders.map((order) => (
              <Card key={order.id}>
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-3">
                      {order.orderName}
                    </h3>
                    <p className="text-gray-700 mb-2 font-medium">
                      금액: {order.amount.toLocaleString()}원
                    </p>
                    <p className="text-sm text-gray-500">
                      {order.createdAt
                        ? new Date(order.createdAt).toLocaleDateString(
                            "ko-KR",
                            {
                              year: "numeric",
                              month: "long",
                              day: "numeric",
                            }
                          )
                        : "날짜 정보 없음"}
                    </p>
                  </div>
                  <span
                    className={`px-4 py-2 rounded-xl text-sm font-medium ml-4 ${
                      order.status === "PAID"
                        ? "bg-[#FFF4D6] text-gray-900"
                        : order.status === "CANCELED"
                        ? "bg-red-50 text-red-600 border border-red-200"
                        : order.status === "FAILED"
                        ? "bg-red-50 text-red-600 border border-red-200"
                        : order.status === "PENDING"
                        ? "bg-[#FFF4D6] text-gray-900 border border-[#FFC837]"
                        : "bg-gray-50 text-gray-600 border border-gray-200"
                    }`}
                  >
                    {getStatusDisplay(order.status)}
                  </span>
                </div>
              </Card>
            ))}
          </div>
          {orders && (
            <div className="mt-12">
              <Pagination
                currentPage={currentPage}
                totalPages={orders.totalPages}
                onPageChange={setCurrentPage}
              />
            </div>
          )}
        </>
      ) : (
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-12 text-center">
          <p className="text-gray-500">주문 내역이 없습니다.</p>
        </div>
      )}
    </div>
  );
}