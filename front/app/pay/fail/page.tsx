'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import ErrorState from '@/src/components/common/ErrorState';

export default function PaymentFailPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const code = searchParams.get('code');
  const message = searchParams.get('message');
  const orderId = searchParams.get('orderId');

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="text-center p-12 max-w-md">
        <div className="mb-6">
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100">
            <svg
              className="h-8 w-8 text-red-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </div>
        </div>
        <h1 className="text-2xl font-bold text-gray-900 mb-4">결제가 실패했습니다</h1>
        <div className="text-left space-y-2 mb-6">
          {code && (
            <p>
              <span className="font-medium">에러 코드:</span> {code}
            </p>
          )}
          {message && (
            <p>
              <span className="font-medium">에러 메시지:</span> {message}
            </p>
          )}
          {orderId && (
            <p>
              <span className="font-medium">주문 ID:</span> {orderId}
            </p>
          )}
        </div>
        <div className="space-y-3">
          <Button onClick={() => router.push('/me/orders')} className="w-full">
            주문 내역으로 이동
          </Button>
          <Button onClick={() => router.push('/')} variant="secondary" className="w-full">
            홈으로 이동
          </Button>
        </div>
      </Card>
    </div>
  );
}

