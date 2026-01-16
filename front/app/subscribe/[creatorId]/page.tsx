"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Script from "next/script";
import { orderApi } from "@/src/api/orderApi";
import Button from "@/src/components/common/Button";
import ErrorState from "@/src/components/common/ErrorState";
import Card from "@/src/components/common/Card";

declare global {
  interface Window {
    TossPayments: any;
  }
}

export default function SubscribePage() {
  const params = useParams();
  const router = useRouter();
  const creatorId = Number(params.creatorId);
  const [selectedPeriod, setSelectedPeriod] = useState<1 | 3 | 12>(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tossPayments, setTossPayments] = useState<any>(null);
  const [customerKey, setCustomerKey] = useState<string>("");

  const clientKey =
    process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY ||
    "test_ck_4yKeq5bgrpLAXPeBzqK4rGX0lzW6";
  const redirectMode = process.env.NEXT_PUBLIC_TOSS_REDIRECT_MODE || "frontend";

  // 금액 계산 (임시 하드코딩)
  const getAmount = (period: number) => {
    const baseAmount = 10000; // 1개월 기준
    return baseAmount * period;
  };

  useEffect(() => {
    // 사용자 정보 가져오기 (customerKey용)
    const fetchUserInfo = async () => {
      try {
        const { userApi } = await import("@/src/api/userApi");
        const user = await userApi.getMe();
        setCustomerKey(`user-${user.id}`);
      } catch (err) {
        // 로그인 안 된 경우 기본값 사용
        setCustomerKey(`user-${creatorId}`);
      }
    };
    fetchUserInfo();
  }, [creatorId]);

  useEffect(() => {
    // TossPayments SDK 로드 후 초기화
    if (typeof window !== "undefined" && window.TossPayments) {
      const instance = window.TossPayments(clientKey);
      setTossPayments(instance);
    }
  }, [clientKey]);

  const handlePayment = async () => {
    if (!tossPayments) {
      setError("결제 시스템을 초기화하는 중입니다. 잠시 후 다시 시도해주세요.");
      return;
    }

    setError(null);
    setLoading(true);

    try {
      // 1. 주문 생성
      const amount = getAmount(selectedPeriod);
      const orderName = `멤버십 ${selectedPeriod}개월`;
      const order = await orderApi.createOrder({
        creatorId,
        orderName,
        amount,
      });

      // 2. 결제 요청
      const successUrl =
        redirectMode === "frontend"
          ? `${window.location.origin}/pay/success`
          : `${
              process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
            }/confirm`;
      const failUrl =
        redirectMode === "frontend"
          ? `${window.location.origin}/pay/fail`
          : `${
              process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
            }/fail`;

      // Toss Payments SDK v2는 payment() 메서드를 통해 결제 객체를 얻은 후 requestPayment 호출
      const payment = tossPayments.payment({
        customerKey: customerKey || `user-${creatorId}`,
      });

      await payment.requestPayment({
        method: "CARD",
        amount: {
          value: amount,
          currency: "KRW",
        },
        orderId: order.orderId,
        orderName,
        successUrl,
        failUrl,
      });
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "결제 요청에 실패했습니다."
      );
      setLoading(false);
    }
  };

  return (
    <>
      <Script
        src="https://js.tosspayments.com/v2/standard"
        strategy="lazyOnload"
        onLoad={() => {
          if (typeof window !== "undefined" && window.TossPayments) {
            const instance = window.TossPayments(clientKey);
            setTossPayments(instance);
          }
        }}
      />
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">멤버십 가입</h1>

        {error && <ErrorState message={error} />}

        <Card>
          <h2 className="text-xl font-semibold text-gray-900 mb-6">
            기간 선택
          </h2>
          <div className="text-gray-900 space-y-4">
            {[1, 3, 12].map((period) => (
              <label
                key={period}
                className={`flex items-center justify-between p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                  selectedPeriod === period
                    ? "border-blue-600 bg-blue-50"
                    : "border-gray-300 hover:border-gray-400"
                }`}
              >
                <div>
                  <input
                    type="radio"
                    value={period}
                    checked={selectedPeriod === period}
                    onChange={(e) =>
                      setSelectedPeriod(Number(e.target.value) as 1 | 3 | 12)
                    }
                    className="mr-3"
                  />
                  <span className="font-medium">{period}개월</span>
                </div>
                <span className="text-lg font-semibold text-gray-900">
                  {getAmount(period).toLocaleString()}원
                </span>
              </label>
            ))}
          </div>

          <div className="mt-8 pt-6 border-t">
            <div className="flex justify-between items-center mb-6">
              <span className="text-lg font-semibold text-gray-900">
                총 결제 금액
              </span>
              <span className="text-2xl font-bold text-blue-600">
                {getAmount(selectedPeriod).toLocaleString()}원
              </span>
            </div>
            <Button
              onClick={handlePayment}
              disabled={loading || !tossPayments}
              className="w-full"
            >
              {loading ? "처리 중..." : "결제하기"}
            </Button>
          </div>
        </Card>
      </div>
    </>
  );
}
