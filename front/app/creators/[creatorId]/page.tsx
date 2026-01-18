"use client";

import { useEffect, useState, useCallback } from "react";
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

        // 본인 페이지가 아닌 경우에만 구독 정보 확인
        if (!isOwnPage) {
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
          // 본인 페이지인 경우 크리에이터 이름 설정
          setCreatorName(currentUser.nickname);
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
          // 403 에러면 구독 필요 (본인 페이지가 아닌 경우에만)
          if (!isOwnPage && postErr.response?.status === 403) {
            setPosts(null);
            // 백엔드에서 보낸 에러 메시지 사용
            setSubscriptionErrorMessage(
              postErr.response?.data?.message ||
                "구독(팔로우)이 필요한 게시글입니다."
            );
          } else if (isOwnPage) {
            // 본인 페이지인 경우 게시글 로드 실패는 무시 (에러 처리 안 함)
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

  // 게시글 필터링 및 권한 처리
  const getFilteredPosts = () => {
    if (!posts) return [];

    // 본인 페이지인 경우 모든 게시글 표시
    if (isOwnPage) {
      return posts.content;
    }

    if (!isLoggedIn || !isSubscribed) {
      // 구독 안했으면 게시글 안보임
      return [];
    }

    // 구독했지만 멤버십이 아니면 전체공개만 보임
    // SubscribeType이 PAID면 CANCELED 상태여도 멤버십 전용 글을 볼 수 있음
    if (subscribeType === "FREE") {
      return posts.content.filter((post) => post.visibility === "PUBLIC");
    }

    // 멤버십(PAID)이면 다 보임 (CANCELED 상태여도 SubscribeType이 PAID면 볼 수 있음)
    return posts.content;
  };

  const filteredPosts = getFilteredPosts();
  // SubscribeType이 PAID면 멤버십 전용 글을 볼 수 있음 (Status는 무시)
  const hasMembership = subscribeType === "PAID";

  return (
    <div className="max-w-4xl mx-auto px-6 py-12">
      {/* 프로필 및 구독 버튼 영역 */}
      <div className="mb-12 pb-8 border-b border-gray-100">
        <div className="flex items-center gap-8 mb-6">
          {/* 프로필 */}
          <CreatorProfileImage 
            creatorId={creatorId} 
            nickname={creatorName || `크리에이터`}
            size="md"
          />
          <div className="flex-1">
            <h1 className="text-3xl font-normal text-gray-900 mb-2">
              {creatorName || `크리에이터 #${creatorId}`}
            </h1>
          </div>
        </div>

        {/* 구독 버튼 영역 (로그인 시에만 표시, 본인 페이지가 아닐 때만) */}
        {isLoggedIn && !isOwnPage && (
          <div className="flex gap-3">
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

            {isSubscribed && (
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

      {/* 게시글 목록 */}
      {!isLoggedIn ? (
        <div className="py-16 text-center">
          <p className="text-gray-500">
            로그인 후 게시글을 확인할 수 있습니다.
          </p>
        </div>
      ) : isOwnPage ? (
        // 본인 페이지인 경우 게시글 표시
        posts && posts.content.length > 0 ? (
          <div className="space-y-0 border-t border-gray-100">
            {posts.content.map((post) => (
              <Card
                key={post.id}
                onClick={() => {
                  router.push(`/posts/${post.id}`);
                }}
                className="relative cursor-pointer"
              >
                <h3 className="text-2xl font-normal text-gray-900 mb-3 leading-tight">
                  {post.title}
                </h3>
                <p className="text-gray-600 mb-6 line-clamp-3 leading-relaxed">
                  {post.content}
                </p>
                <div className="flex justify-between items-center text-sm text-gray-500">
                  <span className="font-normal">
                    {post.visibility === "PUBLIC" ? "전체공개" : "멤버십전용"}
                  </span>
                  <span className="font-normal">
                    {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                      year: "numeric",
                      month: "long",
                      day: "numeric",
                    })}
                  </span>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <div className="py-16 text-center">
            <p className="text-gray-500">게시글이 없습니다.</p>
          </div>
        )
      ) : !isSubscribed ? (
        <div className="py-16 text-center">
          <p className="text-gray-500">
            {subscriptionErrorMessage || "구독(팔로우)이 필요한 게시글입니다."}
          </p>
        </div>
      ) : posts && posts.content.length > 0 ? (
        <div className="space-y-0 border-t border-gray-100">
          {posts.content.map((post) => {
            const canView = post.visibility === "PUBLIC" || hasMembership;
            const isBlurred =
              post.visibility === "SUBSCRIBERS_ONLY" && !hasMembership;

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
                className="relative cursor-pointer"
              >
                <div className={isBlurred ? "blur-sm pointer-events-none" : ""}>
                  <h3 className="text-2xl font-normal text-gray-900 mb-3 leading-tight">
                    {post.title}
                  </h3>
                  <p className="text-gray-600 mb-6 line-clamp-3 leading-relaxed">
                    {post.content}
                  </p>
                  <div className="flex justify-between items-center text-sm text-gray-500">
                    <span className="font-normal">
                      {post.visibility === "PUBLIC" ? "전체공개" : "멤버십전용"}
                    </span>
                    <span className="font-normal">
                      {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </span>
                  </div>
                </div>
                {isBlurred && (
                  <div className="absolute inset-0 flex items-center justify-center bg-white bg-opacity-95">
                    <div className="bg-white border border-gray-200 px-6 py-3">
                      <p className="text-gray-600 font-normal">
                        멤버십 회원만 볼 수 있는 글입니다
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
          <p className="text-gray-500">게시글이 없습니다.</p>
        </div>
      )}
    </div>
  );
}
