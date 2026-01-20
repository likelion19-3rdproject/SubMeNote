'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { adminApi } from '@/src/api/adminApi';
import { HiddenCommentResponse } from '@/src/types/report';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';

export default function HiddenCommentsPage() {
  const router = useRouter();
  const [comments, setComments] = useState<Page<HiddenCommentResponse> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processing, setProcessing] = useState<number | null>(null);

  const loadComments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await adminApi.getHiddenComments(currentPage, 20);
      setComments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '신고 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadComments();
  }, [loadComments]);

  const handleRestore = async (commentId: number) => {
    if (!confirm('이 댓글을 복구하시겠습니까?')) return;

    try {
      setProcessing(commentId);
      await adminApi.restoreContent({ targetId: commentId, type: 'COMMENT' });
      alert('댓글이 복구되었습니다.');
      loadComments();
    } catch (err: any) {
      alert(err.response?.data?.message || '복구에 실패했습니다.');
    } finally {
      setProcessing(null);
    }
  };

  const handleDelete = async (commentId: number) => {
    if (!confirm('이 댓글을 영구적으로 삭제하시겠습니까?')) return;

    try {
      setProcessing(commentId);
      await adminApi.deleteContent({ targetId: commentId, type: 'COMMENT' });
      alert('댓글이 삭제되었습니다.');
      loadComments();
    } catch (err: any) {
      alert(err.response?.data?.message || '삭제에 실패했습니다.');
    } finally {
      setProcessing(null);
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-12">
        <ErrorState message={error} onRetry={loadComments} />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-12 animate-fade-in-scale">
      <h1 className="text-4xl font-black text-white mb-10"><span>💬</span> <span className="gradient-text">신고된 댓글 관리</span></h1>
      <p className="text-gray-400 text-lg mb-8">신고된 댓글을 확인하고 삭제하거나 복구할 수 있습니다.</p>

      {comments && comments.content.length > 0 ? (
        <>
          <div className="space-y-4">
            {comments.content.map((comment) => (
              <Card key={comment.commentId}>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-xs bg-red-500/20 text-red-400 border border-red-500/30 px-2 py-1 rounded-full font-bold">
                        {comment.status}
                      </span>
                    </div>
                    <p className="text-white mb-2">{comment.content}</p>
                    <p className="text-sm text-gray-300 mb-1">
                      작성자: {comment.nickName}
                    </p>
                    <p className="text-sm text-gray-400">
                      작성일: {new Date(comment.createdAt).toLocaleDateString('ko-KR', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                      })}
                    </p>
                  </div>

                  <div className="flex gap-2">
                    <Button
                      onClick={() => router.push(`/posts/${comment.postId}`)}
                      variant="secondary"
                      size="sm"
                    >
                      게시글 보기
                    </Button>
                    <Button
                      onClick={() => handleRestore(comment.commentId)}
                      disabled={processing === comment.commentId}
                      size="sm"
                    >
                      복구
                    </Button>
                    <Button
                      onClick={() => handleDelete(comment.commentId)}
                      variant="danger"
                      disabled={processing === comment.commentId}
                      size="sm"
                    >
                      삭제
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {comments.totalPages > 1 && (
            <div className="mt-8">
              <Pagination
                currentPage={currentPage}
                totalPages={comments.totalPages}
                onPageChange={setCurrentPage}
              />
            </div>
          )}
        </>
      ) : (
        <Card>
          <div className="text-center py-8">
            <div className="glass p-12 text-center rounded-2xl border border-purple-400/20 animate-fade-in-scale">
              <div className="text-7xl mb-6 animate-pulse">📭</div>
              <p className="text-gray-400 text-xl font-bold">신고된 댓글이 없습니다.</p>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
