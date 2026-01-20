'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { userApi } from '@/src/api/userApi';
import { CommentResponseDto } from '@/src/types/comment';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';

export default function CommentsPage() {
  const router = useRouter();
  const [comments, setComments] = useState<Page<CommentResponseDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadComments();
  }, []);

  const loadComments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userApi.getMyComments();
      setComments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '댓글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

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
        <ErrorState message={error} onRetry={loadComments} />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-16">
      <h1 className="text-3xl font-bold text-gray-900 mb-12">내가 작성한 댓글</h1>

      {comments && comments.content.length > 0 ? (
        <div className="grid gap-4">
          {comments.content.map((comment) => (
            <Card
              key={comment.id}
              onClick={() => router.push(`/posts/${comment.postId}`)}
            >
              <div className="mb-3">
                <p className="text-sm text-gray-500 mb-2 font-medium">
                  게시글: {comment.postTitle}
                </p>
              </div>
              <p className="text-gray-700 mb-3 leading-relaxed">{comment.content}</p>
              <div className="flex justify-between items-center text-sm text-gray-500">
                <span className="font-medium">작성일: {new Date(comment.createdAt).toLocaleDateString()}</span>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-12 text-center">
          <p className="text-gray-500">작성한 댓글이 없습니다.</p>
        </div>
      )}
    </div>
  );
}

