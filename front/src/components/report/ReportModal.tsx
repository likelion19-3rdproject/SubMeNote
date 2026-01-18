'use client';

import { useState } from 'react';
import { reportApi } from '@/src/api/reportApi';
import { ReportType } from '@/src/types/report';
import Button from '@/src/components/common/Button';
import Textarea from '@/src/components/common/Textarea';

interface ReportModalProps {
  targetId: number;
  type: ReportType;
  onClose: () => void;
  onSuccess?: () => void;
}

export default function ReportModal({ targetId, type, onClose, onSuccess }: ReportModalProps) {
  const [customReason, setCustomReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!customReason.trim()) {
      alert('신고 사유를 입력해주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await reportApi.createReport({
        targetId,
        type,
        customReason: customReason.trim(),
      });
      alert('신고가 접수되었습니다.');
      onSuccess?.();
      onClose();
    } catch (err: any) {
      alert(err.response?.data?.message || '신고에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 className="text-xl font-bold text-gray-900 mb-4">
          {type === 'POST' ? '게시글' : '댓글'} 신고
        </h3>

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              신고 사유
            </label>
            <Textarea
              value={customReason}
              onChange={(e) => setCustomReason(e.target.value)}
              placeholder="신고 사유를 상세히 입력해주세요..."
              rows={5}
              disabled={submitting}
              required
            />
            <p className="text-xs text-gray-500 mt-1">
              허위 신고 시 제재를 받을 수 있습니다.
            </p>
          </div>

          <div className="flex gap-3">
            <Button
              type="button"
              onClick={onClose}
              variant="secondary"
              className="flex-1"
              disabled={submitting}
            >
              취소
            </Button>
            <Button
              type="submit"
              variant="danger"
              className="flex-1"
              disabled={submitting}
            >
              {submitting ? '신고 중...' : '신고하기'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
