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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <ErrorState message={error} onRetry={loadPosts} />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in-scale">
      <h1 className="text-4xl font-black text-white mb-10"><span>📝</span> <span className="gradient-text">내 게시글</span></h1>

      {posts && posts.content.length > 0 ? (
        <div className="space-y-4">
          {posts.content.map((post) => (
            <Card
              key={post.id}
              onClick={() => router.push(`/posts/${post.id}`)}
              className="hover:shadow-lg transition-shadow cursor-pointer"
            >
              <h2 className="text-2xl font-semibold text-white mb-2">{post.title}</h2>
              <p className="text-gray-300 mb-4 line-clamp-3">{post.content}</p>
              <div className="flex justify-between items-center text-sm text-gray-400">
                <span>공개 범위: {post.visibility === 'PUBLIC' ? '전체공개' : '멤버십전용'}</span>
                <span>{new Date(post.createdAt).toLocaleDateString()}</span>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <div className="glass p-12 text-center rounded-2xl border border-purple-400/20 animate-fade-in-scale">
          <div className="text-7xl mb-6 animate-pulse">📭</div>
          <p className="text-gray-400 text-xl font-bold">작성한 게시글이 없습니다.</p>
        </div>
      )}
    </div>
  );
}

