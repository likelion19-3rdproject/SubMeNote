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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <ErrorState message={error} onRetry={loadComments} />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in-scale">
      <h1 className="text-4xl font-black text-white mb-10"><span>💬</span> <span className="gradient-text">내가 작성한 댓글</span></h1>

      {comments && comments.content.length > 0 ? (
        <div className="space-y-4">
          {comments.content.map((comment) => (
            <Card
              key={comment.id}
              onClick={() => router.push(`/posts/${comment.postId}`)}
              className="hover:shadow-lg transition-shadow cursor-pointer"
            >
              <div className="mb-3">
                <p className="text-sm text-gray-400 mb-1">
                  게시글: {comment.postTitle}
                </p>
              </div>
              <p className="text-white mb-2">{comment.content}</p>
              <div className="flex justify-between items-center text-sm text-gray-400">
                <span>작성일: {new Date(comment.createdAt).toLocaleDateString()}</span>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <div className="glass p-12 text-center rounded-2xl border border-purple-400/20 animate-fade-in-scale">
          <div className="text-7xl mb-6 animate-pulse">📭</div>
          <p className="text-gray-400 text-xl font-bold">작성한 댓글이 없습니다.</p>
        </div>
      )}
    </div>
  );
}

