'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { postApi } from '@/src/api/postApi';
import { subscribeApi } from '@/src/api/subscribeApi';
import { PostResponseDto } from '@/src/types/post';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Input from '@/src/components/common/Input';
import Button from '@/src/components/common/Button';

export default function FeedPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<Page<PostResponseDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [membershipCreatorIds, setMembershipCreatorIds] = useState<Set<number>>(new Set());
  const [searchKeyword, setSearchKeyword] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  // 컴포넌트 레벨로 loadPosts 함수 이동
  const loadPosts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // 1. 내가 구독한 크리에이터 목록 조회 (멤버십 타입 확인용)
      try {
        const subscribedData = await subscribeApi.getMyCreators(0, 100);
        const membershipIds = new Set(
          subscribedData.content
            .filter((sub) => sub.type === 'PAID' && sub.status === 'ACTIVE')
            .map((sub) => sub.creatorId)
        );
        setMembershipCreatorIds(membershipIds);
      } catch (err) {
        // 구독 정보 조회 실패해도 게시글은 로드 시도
        console.error('구독 정보 조회 실패:', err);
      }

      // 2. 구독 피드 게시글 조회 (검색 중이면 검색 API, 아니면 일반 API)
      let data;
      if (isSearching && searchKeyword.trim()) {
        data = await postApi.searchSubscribedPosts(searchKeyword.trim(), 0, 100);
      } else {
        data = await postApi.getPosts();
      }
      setPosts(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [isSearching, searchKeyword]);

  useEffect(() => {
    loadPosts();
  }, [loadPosts]);

  const handleSearch = () => {
    if (searchKeyword.trim()) {
      setIsSearching(true);
    }
  };

  const handleClearSearch = () => {
    setSearchKeyword("");
    setIsSearching(false);
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

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

      {/* 검색 영역 */}
      <div className="mb-8">
        <div className="flex gap-3">
          <div className="flex-1">
            <Input
              type="text"
              placeholder="게시글 검색..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={handleKeyPress}
              className="text-gray-500"
            />
          </div>
          <Button onClick={handleSearch} disabled={!searchKeyword.trim()}>
            검색
          </Button>
          {isSearching && (
            <Button variant="secondary" onClick={handleClearSearch}>
              전체 보기
            </Button>
          )}
        </div>
        {isSearching && (
          <p className="text-sm text-gray-500 mt-2">
            &quot;{searchKeyword}&quot; 검색 결과
          </p>
        )}
      </div>

      {posts && posts.content.length > 0 ? (
        <div className="space-y-0 border-t border-gray-100">
          {posts.content.map((post) => {
            // 멤버십 전용 게시글인지 확인
            const isMembershipOnly = post.visibility === 'SUBSCRIBERS_ONLY';
            // 해당 크리에이터의 멤버십에 가입했는지 확인
            const hasMembership = membershipCreatorIds.has(post.userId);
            // 볼 수 있는 권한이 있는지
            const canView = !isMembershipOnly || hasMembership;
            // 흐림 처리할지 여부
            const isBlurred = isMembershipOnly && !hasMembership;

            return (
              <Card
                key={post.id}
                onClick={() => {
                  if (canView) {
                    router.push(`/posts/${post.id}`);
                  } else {
                    router.push(`/subscribe/${post.userId}`);
                  }
                }}
                className="relative cursor-pointer"
              >
                {/* 작성자 정보는 항상 명확하게 보이도록 상단에 배치 */}
                <div className="flex items-center gap-2 mb-3 text-sm text-gray-500">
                  <span className="font-normal">{post.nickname}</span>
                  {isMembershipOnly && (
                    <span className="text-xs bg-purple-100 text-purple-700 px-2 py-0.5 rounded">
                      멤버십
                    </span>
                  )}
                  <span className="ml-auto font-normal">
                    {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                      year: "numeric",
                      month: "long",
                      day: "numeric",
                    })}
                  </span>
                </div>

                {/* 제목과 내용만 blur 처리 */}
                <div className={isBlurred ? "blur-sm pointer-events-none" : ""}>
                  <h2 className="text-2xl font-normal text-gray-900 mb-3 leading-tight">
                    {post.title}
                  </h2>
                  <p className="text-gray-600 mb-4 line-clamp-3 leading-relaxed">
                    {post.content}
                  </p>
                  <div className="flex items-center gap-1 text-sm text-gray-500">
                    <span>{post.likedByMe ? '❤️' : '🤍'}</span>
                    <span>{post.likeCount}</span>
                  </div>
                </div>

                {isBlurred && (
                  <div className="absolute top-[60px] left-0 right-0 bottom-0 flex items-center justify-center bg-white bg-opacity-95">
                    <div className="bg-white border border-gray-200 px-6 py-4 text-center">
                      <p className="text-gray-600 font-normal mb-2">
                        멤버십 회원만 볼 수 있는 글입니다
                      </p>
                      <p className="text-sm text-gray-500">
                        클릭하여 멤버십 가입하기
                      </p>
                    </div>
                  </div>
                )}
              </Card>
            );
          })}
        </div>
      ) : (
        <p className="text-gray-500 py-8">구독한 크리에이터의 게시글이 없습니다.</p>
      )}
    </div>
  );
}

