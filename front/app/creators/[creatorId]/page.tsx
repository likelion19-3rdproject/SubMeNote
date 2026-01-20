"use client";

import { useEffect, useState, useCallback, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import { postApi } from "@/src/api/postApi";
import { subscribeApi } from "@/src/api/subscribeApi";
import { homeApi } from "@/src/api/homeApi";
import { userApi } from "@/src/api/userApi";
import { PostResponseDto } from "@/src/types/post";
import { Page } from "@/src/types/common";
import Card from "@/src/components/common/Card";
import { SubscribedCreatorResponseDto } from "@/src/types/subscribe";
import LoadingSpinner from "@/src/components/common/LoadingSpinner";
import ErrorState from "@/src/components/common/ErrorState";
import Button from "@/src/components/common/Button";
import CreatorProfileImage from "@/src/components/common/CreatorProfileImage";
import Input from "@/src/components/common/Input";

export default function CreatorPage() {
  const params = useParams();
  const router = useRouter();
  const creatorId = Number(params.creatorId);
  const [posts, setPosts] = useState<Page<PostResponseDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [subscribeId, setSubscribeId] = useState<number | null>(null);
  const [subscribeType, setSubscribeType] = useState<"FREE" | "PAID" | null>(
    null
  );
  const [isMembershipCanceled, setIsMembershipCanceled] = useState(false);
  const [subscribing, setSubscribing] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [creatorName, setCreatorName] = useState<string>("");
  const [subscriptionErrorMessage, setSubscriptionErrorMessage] = useState<
    string | null
  >(null);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const isOwnPage = currentUserId !== null && currentUserId === creatorId;

  const loadData = useCallback(async () => {
    if (!creatorId) return;

    try {
      setLoading(true);
      setError(null);
      
      let tempCreatorName = "";

      // 로그인 상태 확인 및 구독 상태 확인
      try {
        // 현재 로그인한 사용자 정보 가져오기
        const currentUser = await userApi.getMe();
        setCurrentUserId(currentUser.id);
        setIsAdmin(currentUser.roles.includes('ROLE_ADMIN'));
        const isOwnPage = currentUser.id === creatorId;

        setIsLoggedIn(true);
        
        // const subscribed: SubscribedCreatorResponseDto | undefined =
        //   subscribedData.content.find((c) => c.creatorId === creatorId);
        // if (subscribed) {
        //   setIsSubscribed(true);
        //   setSubscribeId(subscribed.subscriptionId);
        //   setSubscribeType(subscribed.type);
        //   setCreatorName(subscribed.creatorNickname);
        //   tempCreatorName = subscribed.creatorNickname;
        //   // 멤버십 해지 상태 확인 (PAID 타입이고 status가 CANCELED면 해지됨)
        //   setIsMembershipCanceled(
        //     subscribed.type === "PAID" && subscribed.status === "CANCELED"
        //   );

        // 본인 페이지가 아니고 어드민이 아닌 경우에만 구독 정보 확인
        if (!isOwnPage && !currentUser.roles.includes('ROLE_ADMIN')) {
          const subscribedData = await subscribeApi.getMyCreators(0, 100);
          const subscribed: SubscribedCreatorResponseDto | undefined =
            subscribedData.content.find((c) => c.creatorId === creatorId);
          if (subscribed) {
            setIsSubscribed(true);
            setSubscribeId(subscribed.subscriptionId);
            setSubscribeType(subscribed.type);
            setCreatorName(subscribed.creatorNickname);
            // 멤버십 해지 상태 확인 (PAID 타입이고 status가 CANCELED면 해지됨)
            setIsMembershipCanceled(
              subscribed.type === "PAID" && subscribed.status === "CANCELED"
            );
          } else {
            setIsSubscribed(false);
            setSubscribeId(null);
            setSubscribeType(null);
            setIsMembershipCanceled(false);
          }
        } else {
          // 본인 페이지이거나 어드민인 경우 크리에이터 이름 설정
          setCreatorName(currentUser.nickname);
          // 어드민인 경우 구독 상태를 true로 설정 (게시글 조회를 위해)
          if (currentUser.roles.includes('ROLE_ADMIN') && !isOwnPage) {
            setIsSubscribed(true);
          }
        }

        // 게시글 로드 시도
        try {
          const postsData = await postApi.getPostsByCreator(creatorId);
          setPosts(postsData);
          setSubscriptionErrorMessage(null);
          
          // 게시글이 있으면 첫 번째 게시글의 작성자 닉네임을 크리에이터 이름으로 설정
          if (postsData.content.length > 0 && !tempCreatorName) {
            setCreatorName(postsData.content[0].nickname);
            tempCreatorName = postsData.content[0].nickname;
          }
        } catch (postErr: any) {
          // 403 에러면 구독 필요 (본인 페이지가 아니고 어드민이 아닌 경우에만)
          if (!isOwnPage && !currentUser.roles.includes('ROLE_ADMIN') && postErr.response?.status === 403) {
            setPosts(null);
            // 백엔드에서 보낸 에러 메시지 사용
            setSubscriptionErrorMessage(
              postErr.response?.data?.message ||
                "구독(팔로우)이 필요한 게시글입니다."
            );
          } else if (isOwnPage || currentUser.roles.includes('ROLE_ADMIN')) {
            // 본인 페이지이거나 어드민인 경우 게시글 로드 실패는 무시 (에러 처리 안 함)
            setPosts(null);
            setSubscriptionErrorMessage(null);
          } else {
            throw postErr;
          }
        }
      } catch (err: any) {
        // 401 에러면 인증되지 않은 경우
        if (err.response?.status === 401) {
          setIsLoggedIn(false);
          setIsSubscribed(false);
          // 비로그인 시에도 게시글 목록은 로드 시도
          try {
            const postsData = await postApi.getPostsByCreator(creatorId);
            setPosts(postsData);
            
            // 게시글이 있으면 첫 번째 게시글의 작성자 닉네임을 크리에이터 이름으로 설정
            if (postsData.content.length > 0 && !tempCreatorName) {
              setCreatorName(postsData.content[0].nickname);
              tempCreatorName = postsData.content[0].nickname;
            }
          } catch (postErr: any) {
            // 게시글 로드 실패는 무시
            setPosts(null);
          }
        } else {
          throw err;
        }
      }
      
      // 모든 로딩이 끝난 후에도 크리에이터 이름이 없으면 홈 API로 가져오기
      if (!tempCreatorName) {
        try {
          const creatorsData = await homeApi.getCreators(0, 100);
          const creator = creatorsData.content.find(
            (c) => c.creatorId === creatorId
          );
          if (creator) {
            setCreatorName(creator.nickname);
          }
        } catch {
          // 실패해도 무시 (기본값 사용)
        }
      }
    } catch (err: any) {
      setError(
        err.response?.data?.message || "데이터를 불러오는데 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  }, [creatorId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleSubscribe = async () => {
    if (isSubscribed && subscribeId) {
      // 구독 취소
      try {
        setSubscribing(true);
        await subscribeApi.deleteSubscribe(subscribeId);
        setIsSubscribed(false);
        setSubscribeId(null);
        setSubscribeType(null);
        setIsMembershipCanceled(false);
        setPosts(null);
        setSubscriptionErrorMessage(null);
        // 구독 취소 후 게시글 목록 다시 로드 (403 에러 발생 예상)
        await loadData();
      } catch (err: any) {
        alert(err.response?.data?.message || "구독 취소에 실패했습니다.");
      } finally {
        setSubscribing(false);
      }
    } else {
      // 구독하기
      try {
        setSubscribing(true);
        const result = await subscribeApi.subscribe(creatorId);
        setIsSubscribed(true);
        setSubscribeId(result.id);
        setSubscribeType(result.type);
        setCreatorName(result.creatorNickname);
        // 구독 후 게시글 목록 다시 로드
        await loadData();
      } catch (err: any) {
        alert(err.response?.data?.message || "구독에 실패했습니다.");
      } finally {
        setSubscribing(false);
      }
    }
  };

  const handleMembership = async () => {
    if (subscribeType === "PAID" && !isMembershipCanceled) {
      // 멤버십 해지 (status를 CANCELED로 변경)
      if (confirm("멤버십을 해지하시겠습니까?")) {
        try {
          setSubscribing(true);
          if (!subscribeId) return;

          const result = await subscribeApi.updateSubscribe(subscribeId, {
            status: "CANCELED",
          });
          setIsMembershipCanceled(true);
          // 상태 업데이트
          if (result.status === "CANCELED") {
            setIsMembershipCanceled(true);
          }
        } catch (err: any) {
          alert(err.response?.data?.message || "멤버십 해지에 실패했습니다.");
        } finally {
          setSubscribing(false);
        }
      }
    } else if (isMembershipCanceled) {
      // 멤버십 해지 철회 (status를 ACTIVE로 변경)
      try {
        setSubscribing(true);
        if (!subscribeId) return;

        const result = await subscribeApi.updateSubscribe(subscribeId, {
          status: "ACTIVE",
        });
        setIsMembershipCanceled(false);
        // 상태 업데이트
        if (result.status === "ACTIVE") {
          setIsMembershipCanceled(false);
        }
      } catch (err: any) {
        alert(
          err.response?.data?.message || "멤버십 해지 철회에 실패했습니다."
        );
      } finally {
        setSubscribing(false);
      }
    } else {
      // 멤버십 가입
      router.push(`/subscribe/${creatorId}`);
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
        <ErrorState message={error} onRetry={loadData} />
      </div>
    );
  }

  // 게시글 필터링 및 권한 처리 (검색 포함)
  const getFilteredPosts = () => {
    if (!posts) return [];

    let filtered = posts.content;

    // 본인 페이지인 경우 모든 게시글 표시
    if (isOwnPage) {
      // 검색어가 있으면 필터링
      if (searchKeyword.trim()) {
        filtered = filtered.filter(
          (post) =>
            post.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
            post.content.toLowerCase().includes(searchKeyword.toLowerCase())
        );
      }
      return filtered;
    }

    // 어드민이거나 구독한 경우 게시글을 반환
    if (!isLoggedIn || (!isSubscribed && !isAdmin)) {
      // 구독 안했고 어드민도 아니면 게시글 안보임
      return [];
    }

    // 어드민이거나 구독한 경우 모든 게시글을 반환 (블러 처리는 렌더링 단계에서 수행)
    // 검색어가 있으면 필터링
    if (searchKeyword.trim()) {
      filtered = filtered.filter(
        (post) =>
          post.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          post.content.toLowerCase().includes(searchKeyword.toLowerCase())
      );
    }

    return filtered;
  };

  const filteredPosts = getFilteredPosts();
  // SubscribeType이 PAID면 멤버십 전용 글을 볼 수 있음 (Status는 무시)
  const hasMembership = subscribeType === "PAID";

  return (
    <div className="max-w-4xl mx-auto px-6 py-12">
      {/* 프로필 및 구독 버튼 영역 */}
      <div className="mb-12 pb-8 relative">
        {/* 그라데이션 구분선 */}
        <div className="absolute bottom-0 left-0 right-0 h-[2px] bg-gradient-to-r from-transparent via-purple-400/50 to-transparent"></div>
        <div className="absolute bottom-0 left-0 right-0 h-[2px] bg-purple-400/30 blur-sm"></div>
        <div className="flex items-center gap-8 mb-6">
          {/* 프로필 */}
          <CreatorProfileImage 
            creatorId={creatorId} 
            nickname={creatorName || `크리에이터`}
            size="md"
          />
          <div className="flex-1">
            <h1 className="text-3xl font-normal text-white mb-2">
              {creatorName || `크리에이터 #${creatorId}`}
            </h1>
          </div>
        </div>

        {/* 구독 버튼 영역 (로그인 시에만 표시, 본인 페이지가 아니고 어드민이 아닐 때만) */}
        {isLoggedIn && !isOwnPage && !isAdmin && (
          <div className="flex gap-3">
            {isSubscribed && subscribeType === "PAID" && !isMembershipCanceled && (
              <Button
                onClick={() => router.push(`/subscribe/${creatorId}`)}
                variant="primary"
                className="bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-400 hover:to-purple-500 neon-glow"
              >
                ⏰ 멤버십 연장
              </Button>
            )}
            <Button
              onClick={handleSubscribe}
              disabled={subscribing}
              variant={isSubscribed ? "danger" : "primary"}
            >
              {subscribing
                ? "처리 중..."
                : isSubscribed
                ? "구독 취소"
                : "구독하기"}
            </Button>

            {isSubscribed && !(subscribeType === "PAID" && !isMembershipCanceled) && (
              <Button
                onClick={handleMembership}
                variant={
                  isMembershipCanceled
                    ? "secondary"
                    : subscribeType === "PAID"
                    ? "danger"
                    : "secondary"
                }
              >
                {isMembershipCanceled
                  ? "멤버십 해지 철회"
                  : subscribeType === "PAID"
                  ? "멤버십 해지"
                  : "멤버십 가입"}
              </Button>
            )}
          </div>
        )}
      </div>

      {/* 검색 영역 (게시글이 있을 때만) */}
      {((isOwnPage && posts && posts.content.length > 0) ||
        (isLoggedIn && (isSubscribed || isAdmin) && posts && posts.content.length > 0)) && (
        <div className="mb-8">
          <Input
            type="text"
            placeholder="게시글 검색..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="text-gray-500"
          />
          {searchKeyword.trim() && (
            <p className="text-sm text-gray-500 mt-2">
              &quot;{searchKeyword}&quot; 검색 결과: {filteredPosts.length}개
            </p>
          )}
        </div>
      )}

      {/* 게시글 목록 */}
      {!isLoggedIn ? (
        <div className="py-16 text-center">
          <p className="text-gray-500">
            로그인 후 게시글을 확인할 수 있습니다.
          </p>
        </div>
      ) : isOwnPage ? (
        // 본인 페이지인 경우 게시글 표시
        filteredPosts.length > 0 ? (
          <div className="space-y-6">
            {filteredPosts.map((post, index) => (
              <Card
                key={post.id}
                onClick={() => {
                  router.push(`/posts/${post.id}`);
                }}
                className="relative cursor-pointer animate-fade-in-scale"
                style={{animationDelay: `${index * 0.1}s`}}
              >
                {/* 작성자 정보 */}
                <div className="flex items-center gap-3 mb-5">
                  <div className="w-12 h-12 rounded-full bg-gradient-to-r from-purple-500 to-purple-600 flex items-center justify-center text-white font-black shadow-lg neon-glow">
                    {post.nickname.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white">{post.nickname}</span>
                      {post.visibility === "SUBSCRIBERS_ONLY" && (
                        <span className="text-xs bg-gradient-to-r from-purple-500 to-purple-600 text-white px-3 py-1 rounded-full font-bold neon-glow">
                          ⭐ 멤버십
                        </span>
                      )}
                    </div>
                    <span className="text-sm text-gray-400">
                      {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </span>
                  </div>
                </div>

                {/* 제목과 내용 */}
                <h2 className="text-2xl font-black text-white mb-4 leading-tight group-hover:text-transparent group-hover:bg-clip-text group-hover:bg-gradient-to-r group-hover:from-purple-400 group-hover:to-purple-400 transition-all duration-300">
                  {post.title}
                </h2>
                <p className="text-gray-300 mb-5 line-clamp-3 leading-relaxed">
                  {post.content}
                </p>
                <div className="flex items-center gap-2 text-sm">
                  <div className="flex items-center gap-2 px-4 py-2 rounded-full glass border border-purple-400/25 hover:border-purple-400/45 transition-colors">
                    <span className="text-lg">{post.likedByMe ? '❤️' : '🤍'}</span>
                    <span className="font-bold text-white">{post.likeCount}</span>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <div className="py-16 text-center">
            <p className="text-gray-500">
              {searchKeyword.trim()
                ? "검색 결과가 없습니다."
                : "게시글이 없습니다."}
            </p>
          </div>
        )
      ) : !isSubscribed && !isAdmin ? (
        <div className="py-16 text-center">
          <p className="text-gray-500">
            {subscriptionErrorMessage || "구독(팔로우)이 필요한 게시글입니다."}
          </p>
        </div>
      ) : filteredPosts.length > 0 ? (
        <div className="space-y-6">
          {filteredPosts.map((post, index) => {
            // 어드민이거나 전체 공개이거나 멤버십이 있으면 볼 수 있음
            const canView = isAdmin || post.visibility === "PUBLIC" || hasMembership;
            // 어드민이 아니고 멤버십 전용인데 멤버십이 없을 때만 blur
            const isBlurred =
              !isAdmin && post.visibility === "SUBSCRIBERS_ONLY" && !hasMembership;
            const isMembershipOnly = post.visibility === "SUBSCRIBERS_ONLY";

            return (
              <Card
                key={post.id}
                onClick={() => {
                  if (canView) {
                    // 볼 수 있는 권한이 있으면 게시글로 이동
                    router.push(`/posts/${post.id}`);
                  } else if (isBlurred) {
                    // 멤버십 전용 게시글이면 멤버십 가입 페이지로 이동
                    router.push(`/subscribe/${creatorId}`);
                  }
                }}
                className="relative cursor-pointer animate-fade-in-scale"
                style={{animationDelay: `${index * 0.1}s`}}
              >
                {/* 작성자 정보 */}
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
                    <span className="text-sm text-gray-400">
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
                  <p className="text-gray-300 mb-5 line-clamp-3 leading-relaxed">
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
        <div className="py-16 text-center">
          <p className="text-gray-500">
            {searchKeyword.trim()
              ? "검색 결과가 없습니다."
              : "게시글이 없습니다."}
          </p>
        </div>
      )}
    </div>
  );
}
