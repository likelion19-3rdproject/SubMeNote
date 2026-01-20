'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { postApi } from '@/src/api/postApi';
import { subscribeApi } from '@/src/api/subscribeApi';
import { userApi } from '@/src/api/userApi';
import { PostResponseDto } from '@/src/types/post';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Input from '@/src/components/common/Input';

export default function FeedPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<Page<PostResponseDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [membershipCreatorIds, setMembershipCreatorIds] = useState<Set<number>>(new Set());
  const [isAdmin, setIsAdmin] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");

  // 컴포넌트 레벨로 loadPosts 함수 이동
  const loadPosts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // 1. 현재 사용자 정보 조회 (어드민 여부 확인)
      let userIsAdmin = false;
      try {
        const user = await userApi.getMe();
        userIsAdmin = user.roles.includes('ROLE_ADMIN');
        setIsAdmin(userIsAdmin);
      } catch (err) {
        // 로그인 안 된 경우 어드민 아님
        setIsAdmin(false);
      }

      // 2. 내가 구독한 크리에이터 목록 조회 (멤버십 타입 확인용, 어드민이 아닐 때만)
      if (!userIsAdmin) {
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
      }

      // 3. 구독 피드 게시글 조회
      const data = await postApi.getPosts();
      setPosts(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPosts();
  }, [loadPosts]);

  // 게시글 필터링 (클라이언트 사이드)
  const getFilteredPosts = () => {
    if (!posts) return [];

    if (!searchKeyword.trim()) {
      return posts.content;
    }

    return posts.content.filter(
      (post) =>
        post.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        post.content.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        post.nickname.toLowerCase().includes(searchKeyword.toLowerCase())
    );
  };

  const filteredPosts = getFilteredPosts();

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
      <div className="mb-10 animate-slide-in">
        <h1 className="text-5xl font-black mb-3 gradient-text neon-text">
          📰 구독 피드
        </h1>
        <p className="text-gray-400 text-lg">
          구독한 크리에이터들의 최신 소식을 확인하세요
        </p>
      </div>

      {/* 검색 영역 */}
      <div className="mb-10 animate-fade-in-scale">
        <Input
          type="text"
          placeholder="🔍 게시글 검색 (제목, 내용, 작성자)..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        {searchKeyword.trim() && (
          <p className="text-sm text-gray-400 mt-3 font-bold animate-pulse">
            &quot;{searchKeyword}&quot; 검색 결과: {filteredPosts.length}개
          </p>
        )}
      </div>

      {filteredPosts.length > 0 ? (
        <div className="space-y-6">
          {filteredPosts.map((post, index) => {
            // 멤버십 전용 게시글인지 확인
            const isMembershipOnly = post.visibility === 'SUBSCRIBERS_ONLY';
            // 해당 크리에이터의 멤버십에 가입했는지 확인
            const hasMembership = membershipCreatorIds.has(post.userId);
            // 어드민이거나 멤버십 전용이 아니거나 멤버십이 있으면 볼 수 있음
            const canView = isAdmin || !isMembershipOnly || hasMembership;
            // 흐림 처리할지 여부 (어드민이 아니고 멤버십 전용인데 멤버십이 없을 때만)
            const isBlurred = !isAdmin && isMembershipOnly && !hasMembership;

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
                className="relative cursor-pointer animate-fade-in-scale"
                style={{animationDelay: `${index * 0.1}s`}}
              >
                {/* 작성자 정보는 항상 명확하게 보이도록 상단에 배치 */}
                <div className="flex items-center gap-3 mb-5">
                  <div className="w-12 h-12 rounded-full bg-gradient-to-r from-purple-500 to-purple-600 flex items-center justify-center text-white font-black shadow-lg neon-glow">
                    {post.nickname.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white">{post.nickname}</span>
                      {isMembershipOnly && (
                        <span className="text-xs bg-gradient-to-r from-purple-500 to-purple-600 text-white px-3 py-1 rounded-full font-bold neon-glow">
                          ⭐ 멤버십
                        </span>
                      )}
                    </div>
                    <span className="text-sm text-gray-500">
                      {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </span>
                  </div>
                </div>

                {/* 제목과 내용만 blur 처리 */}
                <div className={isBlurred ? "blur-sm pointer-events-none" : ""}>
                  <h2 className="text-2xl font-black text-white mb-4 leading-tight group-hover:text-transparent group-hover:bg-clip-text group-hover:bg-gradient-to-r group-hover:from-purple-400 group-hover:to-purple-400 transition-all duration-300">
                    {post.title}
                  </h2>
                  <p className="text-gray-400 mb-5 line-clamp-3 leading-relaxed">
                    {post.content}
                  </p>
                  <div className="flex items-center gap-2 text-sm">
                    <div className="flex items-center gap-2 px-4 py-2 rounded-full glass border border-purple-400/25 hover:border-purple-400/45 transition-colors">
                      <span className="text-lg">{post.likedByMe ? '❤️' : '🤍'}</span>
                      <span className="font-bold text-white">{post.likeCount}</span>
                    </div>
                  </div>
                </div>

                {isBlurred && (
                  <div className="absolute top-0 left-0 right-0 bottom-0 flex items-center justify-center glass rounded-2xl">
                    <div className="glass px-10 py-8 text-center rounded-2xl border border-purple-400/30 neon-glow animate-pulse">
                      <div className="text-5xl mb-4">🔒</div>
                      <p className="text-white font-black mb-3 text-xl gradient-text">
                        멤버십 회원만 볼 수 있는 글입니다
                      </p>
                      <p className="text-sm text-gray-400 font-medium">
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
        <div className="glass p-12 text-center rounded-2xl border border-purple-400/20 animate-fade-in-scale">
          <div className="text-7xl mb-6 animate-pulse">📭</div>
          <p className="text-gray-400 text-xl font-bold">
            {searchKeyword.trim()
              ? "검색 결과가 없습니다."
              : "구독한 크리에이터의 게시글이 없습니다."}
          </p>
        </div>
      )}
    </div>
  );
}

