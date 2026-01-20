"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Script from "next/script";
import { orderApi } from "@/src/api/orderApi";
import { paymentApi } from "@/src/api/paymentApi";
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
    const baseAmount = 4900; // 1개월 기준
    return baseAmount * period;
  };
  
  // 가격 표시 텍스트
  const getAmountText = (period: number) => {
    if (period === 3 || period === 12) {
      return "미정";
    }
    return `${getAmount(period).toLocaleString()}원`;
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
    // 3개월, 12개월은 추후 지원 예정
    if (selectedPeriod === 3 || selectedPeriod === 12) {
      alert(`${selectedPeriod}개월 멤버십은 추후 지원 예정입니다.`);
      return;
    }

    if (!tossPayments) {
      setError("결제 시스템을 초기화하는 중입니다. 잠시 후 다시 시도해주세요.");
      return;
    }

    setError(null);
    setLoading(true);

    let currentOrderId: string | null = null; // orderId 저장용 변수

    try {
      // 1. 주문 생성
      const amount = getAmount(selectedPeriod);
      const orderName = `멤버십 ${selectedPeriod}개월`;
      const order = await orderApi.createOrder({
        creatorId,
        orderName,
        amount,
      });

      currentOrderId = order.orderId; // orderId 저장

      // 2. 결제 요청
      const successUrl =
        redirectMode === "frontend"
          ? `${window.location.origin}/pay/success`
          : `${
              process.env.NEXT_PUBLIC_API_URL ||
              (typeof window !== "undefined"
                ? `${window.location.protocol}//${window.location.hostname}:8080`
                : "http://back:8080")
            }/confirm`;
      const failUrl =
        redirectMode === "frontend"
          ? `${window.location.origin}/pay/fail`
          : `${
              process.env.NEXT_PUBLIC_API_URL ||
              (typeof window !== "undefined"
                ? `${window.location.protocol}//${window.location.hostname}:8080`
                : "http://back:8080")
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
      // 결제창 닫기 등의 이유로 실패한 경우
      if (currentOrderId) {
        // 백엔드에 취소 처리 요청
        try {
          await paymentApi.failPayment({
            orderId: currentOrderId,
            code: "USER_CANCEL",
            message: "사용자가 결제를 취소했습니다.",
          });
        } catch (cancelError) {
          console.error("주문 취소 처리 실패:", cancelError);
        }
      }

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
                className={`flex items-center justify-between p-4 border-2 rounded-lg ${
                  period === 3 || period === 12
                    ? "opacity-50 cursor-not-allowed"
                    : "cursor-pointer"
                } transition-colors ${
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
                    disabled={period === 3 || period === 12}
                    onChange={(e) =>
                      setSelectedPeriod(Number(e.target.value) as 1 | 3 | 12)
                    }
                    className="mr-3"
                  />
                  <span className="font-medium">
                    {period}개월 {period === 3 || period === 12 ? "(추후 지원 예정)" : ""}
                  </span>
                </div>
                <span className="text-lg font-semibold text-gray-900">
                  {getAmountText(period)}
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
                {getAmountText(selectedPeriod)}
              </span>
            </div>
            <Button
              onClick={handlePayment}
              disabled={loading || !tossPayments || selectedPeriod === 3 || selectedPeriod === 12}
              className="w-full"
            >
              {loading ? "처리 중..." : 
               selectedPeriod === 3 || selectedPeriod === 12 ? "추후 지원 예정" : "결제하기"}
            </Button>
          </div>
        </Card>
      </div>
    </>
  );
}
