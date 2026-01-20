'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { userApi } from '@/src/api/userApi';
import { PostResponseDto } from '@/src/types/post';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';

export default function MyPostsPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<Page<PostResponseDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadPosts();
  }, []);

  const loadPosts = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userApi.getMyPosts();
      setPosts(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '게시글을 불러오는데 실패했습니다.');
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
        <ErrorState message={error} onRetry={loadPosts} />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-16">
      <h1 className="text-3xl font-bold text-gray-900 mb-12">내 게시글</h1>

      {posts && posts.content.length > 0 ? (
        <div className="grid gap-4">
          {posts.content.map((post) => (
            <Card
              key={post.id}
              onClick={() => router.push(`/posts/${post.id}`)}
            >
              <h2 className="text-2xl font-bold text-gray-900 mb-3">{post.title}</h2>
              <p className="text-gray-500 mb-4 line-clamp-3 leading-relaxed">{post.content}</p>
              <div className="flex justify-between items-center text-sm text-gray-500">
                <span className="font-medium">공개 범위: {post.visibility === 'PUBLIC' ? '전체공개' : '멤버십전용'}</span>
                <span className="font-medium">{new Date(post.createdAt).toLocaleDateString()}</span>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-12 text-center">
          <p className="text-gray-500">작성한 게시글이 없습니다.</p>
        </div>
      )}
    </div>
  );
}

