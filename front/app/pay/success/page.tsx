'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { paymentApi } from '@/src/api/paymentApi';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';

export default function PaymentSuccessPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [status, setStatus] = useState<'processing' | 'success' | 'error'>('processing');
  const [error, setError] = useState<string | null>(null);
  const [paymentData, setPaymentData] = useState<any>(null);
  const hasProcessed = useRef(false);

  useEffect(() => {
    const paymentKey = searchParams.get('paymentKey');
    const orderId = searchParams.get('orderId');
    const amount = searchParams.get('amount');

    if (!paymentKey || !orderId || !amount) {
      setStatus('error');
      setError('결제 정보가 올바르지 않습니다.');
      return;
    }

    if (hasProcessed.current) return;
    hasProcessed.current = true;

    const confirmPayment = async () => {
      try {
        const result = await paymentApi.confirmPayment({
          paymentKey,
          orderId: orderId,
          amount: Number(amount),
        });
        setPaymentData(result);
        setStatus('success');
      } catch (err: any) {
        setStatus('error');
        setError(err.response?.data?.message || '결제 확인에 실패했습니다.');
        // 실패 시 fail 페이지로 이동
        setTimeout(() => {
          router.push(`/pay/fail?code=${err.response?.data?.code || 'UNKNOWN'}&message=${encodeURIComponent(err.response?.data?.message || '결제 확인 실패')}&orderId=${orderId}`);
        }, 2000);
      }
    };

    confirmPayment();
  }, [searchParams, router]);

  if (status === 'processing') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="text-center p-12">
          <LoadingSpinner />
          <p className="mt-4 text-lg text-gray-600">결제를 확인하는 중입니다...</p>
        </Card>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="text-center p-12 max-w-md">
          <ErrorState message={error || '결제 확인 중 오류가 발생했습니다.'} />
          <Button onClick={() => router.push('/me/orders')} className="mt-4">
            주문 내역으로 이동
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="text-center p-12 max-w-md">
        <div className="mb-6">
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100">
            <svg
              className="h-8 w-8 text-green-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 13l4 4L19 7"
              />
            </svg>
          </div>
        </div>
        <h1 className="text-2xl font-bold text-gray-900 mb-4">결제가 완료되었습니다!</h1>
        {paymentData && (
          <div className="text-left space-y-2 mb-6">
            <p>
              <span className="font-medium">주문 ID:</span> {paymentData.orderId}
            </p>
            <p>
              <span className="font-medium">결제 금액:</span>{' '}
              {paymentData.amount?.toLocaleString()}원
            </p>
            <p>
              <span className="font-medium">결제 상태:</span> {paymentData.status}
            </p>
          </div>
        )}
        <div className="space-y-3">
          <Button onClick={() => router.push('/me/subscriptions')} className="w-full">
            내 구독 목록으로 이동
          </Button>
          <Button onClick={() => router.push('/')} variant="secondary" className="w-full">
            홈으로 이동
          </Button>
        </div>
      </Card>
    </div>
  );
}

