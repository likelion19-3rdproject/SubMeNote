'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { adminApi } from '@/src/api/adminApi';
import { HiddenPostResponse } from '@/src/types/report';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';

export default function HiddenPostsPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<Page<HiddenPostResponse> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processing, setProcessing] = useState<number | null>(null);

  const loadPosts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await adminApi.getHiddenPosts(currentPage, 20);
      setPosts(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '신고 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadPosts();
  }, [loadPosts]);

  const handleRestore = async (postId: number) => {
    if (!confirm('이 게시글을 복구하시겠습니까?')) return;

    try {
      setProcessing(postId);
      await adminApi.restoreContent({ targetId: postId, type: 'POST' });
      alert('게시글이 복구되었습니다.');
      loadPosts();
    } catch (err: any) {
      alert(err.response?.data?.message || '복구에 실패했습니다.');
    } finally {
      setProcessing(null);
    }
  };

  const handleDelete = async (postId: number) => {
    if (!confirm('이 게시글을 영구적으로 삭제하시겠습니까?')) return;

    try {
      setProcessing(postId);
      await adminApi.deleteContent({ targetId: postId, type: 'POST' });
      alert('게시글이 삭제되었습니다.');
      loadPosts();
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
        <ErrorState message={error} onRetry={loadPosts} />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">신고된 게시글 관리</h1>

      {posts && posts.content.length > 0 ? (
        <>
          <div className="space-y-4">
            {posts.content.map((post) => (
              <Card key={post.postId}>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">
                        {post.title}
                      </h3>
                      <span className="text-xs bg-red-100 text-red-700 px-2 py-1 rounded">
                        {post.status}
                      </span>
                      <span className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded">
                        {post.visibility === 'PUBLIC' ? '전체공개' : '멤버십전용'}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600 mb-1">
                      작성자: {post.nickName}
                    </p>
                    <p className="text-sm text-gray-500">
                      작성일: {new Date(post.createdAt).toLocaleDateString('ko-KR', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                      })}
                    </p>
                  </div>

                  <div className="flex gap-2">
                    <Button
                      onClick={() => router.push(`/posts/${post.postId}`)}
                      variant="secondary"
                      size="sm"
                    >
                      상세보기
                    </Button>
                    <Button
                      onClick={() => handleRestore(post.postId)}
                      disabled={processing === post.postId}
                      size="sm"
                    >
                      복구
                    </Button>
                    <Button
                      onClick={() => handleDelete(post.postId)}
                      variant="danger"
                      disabled={processing === post.postId}
                      size="sm"
                    >
                      삭제
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {posts.totalPages > 1 && (
            <div className="mt-8">
              <Pagination
                currentPage={currentPage}
                totalPages={posts.totalPages}
                onPageChange={setCurrentPage}
              />
            </div>
          )}
        </>
      ) : (
        <Card>
          <div className="text-center py-8">
            <p className="text-gray-500">신고된 게시글이 없습니다.</p>
          </div>
        </Card>
      )}
    </div>
  );
}
