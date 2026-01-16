'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { postApi } from '@/src/api/postApi';
import { PostResponseDto } from '@/src/types/post';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';

export default function FeedPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<Page<PostResponseDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadPosts = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await postApi.getPosts();
        
        if (isMounted) {
          setPosts(data);
        }
      } catch (err: any) {
        if (isMounted) {
          setError(err.response?.data?.message || '게시글을 불러오는데 실패했습니다.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    loadPosts();

    return () => {
      isMounted = false;
    };
  }, []);

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-12">
        <ErrorState message={error} onRetry={loadPosts} />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-12">
      <h1 className="text-sm font-normal text-gray-500 mb-6 uppercase tracking-wider">
        구독 피드
      </h1>

      {posts && posts.content.length > 0 ? (
        <div className="space-y-0 border-t border-gray-100">
          {posts.content.map((post) => (
            <Card
              key={post.id}
              onClick={() => router.push(`/posts/${post.id}`)}
            >
              <h2 className="text-2xl font-normal text-gray-900 mb-3 leading-tight">
                {post.title}
              </h2>
              <p className="text-gray-600 mb-6 line-clamp-3 leading-relaxed">
                {post.content}
              </p>
              <div className="flex justify-between items-center text-sm text-gray-500">
                <span className="font-normal">{post.nickname}</span>
                <span className="font-normal">
                  {new Date(post.createdAt).toLocaleDateString('ko-KR', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </span>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <p className="text-gray-500 py-8">구독한 크리에이터의 게시글이 없습니다.</p>
      )}
    </div>
  );
}

