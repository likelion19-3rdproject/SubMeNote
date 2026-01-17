"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { homeApi } from "@/src/api/homeApi";
import { subscribeApi } from "@/src/api/subscribeApi";
import { CreatorResponseDto } from "@/src/types/home";
import { SubscribedCreatorResponseDto } from "@/src/types/subscribe";
import { Page } from "@/src/types/common";
import Card from "@/src/components/common/Card";
import LoadingSpinner from "@/src/components/common/LoadingSpinner";
import ErrorState from "@/src/components/common/ErrorState";
import Pagination from "@/src/components/common/Pagination";
import CreatorProfileImage from "@/src/components/common/CreatorProfileImage";

export default function HomePage() {
  const router = useRouter();
  const [creators, setCreators] = useState<Page<CreatorResponseDto> | null>(
    null
  );
  const [subscribedCreators, setSubscribedCreators] =
    useState<Page<SubscribedCreatorResponseDto> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // 크리에이터 목록 로드
      const creatorsData = await homeApi.getCreators(currentPage, 10);

      // 로그인 상태 확인 (구독 목록 로드 시도)
      let subscribedData = null;
      try {
        subscribedData = await subscribeApi.getMyCreators(0, 10);
      } catch (err) {
        // 인증되지 않은 경우 무시
      }

      setCreators(creatorsData);
      if (subscribedData) {
        setSubscribedCreators(subscribedData);
        setIsLoggedIn(true);
      } else {
        setIsLoggedIn(false);
      }
    } catch (err: any) {
      setError(err.message || "데이터를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleCreatorClick = (creatorId: number) => {
    if (!isLoggedIn) {
      router.push("/login");
    } else {
      router.push(`/creators/${creatorId}`);
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

  return (
    <div className="max-w-4xl mx-auto px-6 py-12">
      {/* 내가 구독한 크리에이터 (로그인 시) */}
      {isLoggedIn &&
        subscribedCreators &&
        subscribedCreators.content.length > 0 && (
          <div className="mb-16">
            <h2 className="text-sm font-normal text-gray-500 mb-6 uppercase tracking-wider">
              내가 구독한 크리에이터
            </h2>
            <div className="space-y-0 border-t border-gray-100">
              {subscribedCreators.content.map((creator) => (
                <Card
                  key={creator.creatorId}
                  onClick={() => handleCreatorClick(creator.creatorId)}
                  className="flex items-center gap-6 py-6"
                >
                  {/* 프로필 */}
                  <CreatorProfileImage
                    creatorId={creator.creatorId}
                    nickname={creator.creatorNickname}
                    size="sm"
                  />
                  <div className="flex-1">
                    <h3 className="text-lg font-normal text-gray-900 mb-1">
                      {creator.creatorNickname}
                    </h3>
                    <p className="text-sm text-gray-500">
                      {creator.type === "PAID" ? "멤버십" : "일반 구독"}
                    </p>
                  </div>
                </Card>
              ))}
            </div>
          </div>
        )}

      {/* 전체 크리에이터 목록 */}
      <div>
        <h2 className="text-sm font-normal text-gray-500 mb-6 uppercase tracking-wider">
          전체 크리에이터
        </h2>
        {creators && creators.content.length > 0 ? (
          <>
            <div className="space-y-0 border-t border-gray-100">
              {creators.content.map((creator) => (
                <Card
                  key={creator.creatorId}
                  onClick={() => handleCreatorClick(creator.creatorId)}
                  className="flex items-center gap-6 py-6"
                >
                  {/* 프로필 */}
                  <CreatorProfileImage
                    creatorId={creator.creatorId}
                    nickname={creator.nickname}
                    size="sm"
                  />
                  <div className="flex-1">
                    <h3 className="text-lg font-normal text-gray-900">
                      {creator.nickname}
                    </h3>
                  </div>
                </Card>
              ))}
            </div>
            <div className="mt-12">
              <Pagination
                currentPage={currentPage}
                totalPages={creators.totalPages}
                onPageChange={setCurrentPage}
              />
            </div>
          </>
        ) : (
          <p className="text-gray-500 py-8">크리에이터가 없습니다.</p>
        )}
      </div>
    </div>
  );
}
