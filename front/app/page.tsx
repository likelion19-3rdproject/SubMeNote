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
import Input from "@/src/components/common/Input";

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
  const [searchKeyword, setSearchKeyword] = useState("");

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

  // 크리에이터 필터링 (클라이언트 사이드)
  const getFilteredCreators = () => {
    if (!creators) return [];
    
    if (!searchKeyword.trim()) {
      return creators.content;
    }

    return creators.content.filter((creator) =>
      creator.nickname.toLowerCase().includes(searchKeyword.toLowerCase())
    );
  };

  const filteredCreators = getFilteredCreators();

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
    <div className="max-w-4xl mx-auto px-6 py-16">
      {/* 히어로 섹션 */}
      <div className="mb-20 text-center">
        <div className="mb-6">
          {/* 로고 아이콘 */}
          <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-[#FFC837] to-[#FF9500] rounded-2xl mb-6 shadow-lg">
            <svg
              className="w-12 h-12 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
          </div>
        </div>
        <h1 className="text-5xl font-bold text-gray-900 mb-4 tracking-tight">
          SubMeNote
        </h1>
        <p className="text-xl text-gray-600 mb-8 font-medium">
          구독 기반 유료 글 플랫폼
        </p>
        <div className="flex gap-4 justify-center">
          <button
            onClick={() => {
              if (isLoggedIn) {
                router.push('/feed');
              } else {
                router.push('/login');
              }
            }}
            className="bg-[#FFC837] hover:bg-[#FFB800] text-gray-900 px-8 py-3 text-base font-semibold rounded-xl transition-all duration-200 shadow-sm hover:shadow"
          >
            {isLoggedIn ? '구독 피드 보기' : '크리에이터 둘러보기'}
          </button>
          {isLoggedIn && (
            <button
              onClick={() => router.push('/me')}
              className="bg-white hover:bg-gray-50 text-gray-900 border-2 border-gray-200 hover:border-gray-300 px-8 py-3 text-base font-semibold rounded-xl transition-all duration-200"
            >
              마이페이지
            </button>
          )}
        </div>
      </div>

      {/* 내가 구독한 크리에이터 (로그인 시, 검색 중이 아닐 때만) */}
      {!searchKeyword.trim() && isLoggedIn && (
        <div className="mb-16">
          <h2 className="text-sm font-semibold text-gray-600 mb-6 uppercase tracking-wider">
            내가 구독한 크리에이터
          </h2>
          {subscribedCreators && subscribedCreators.content.length > 0 ? (
            <div className="grid gap-4">
              {subscribedCreators.content.map((creator) => (
                <Card
                  key={creator.creatorId}
                  onClick={() => handleCreatorClick(creator.creatorId)}
                  className="flex items-center gap-6"
                >
                  {/* 프로필 */}
                  <CreatorProfileImage
                    creatorId={creator.creatorId}
                    nickname={creator.creatorNickname}
                    size="sm"
                  />
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-1">
                      {creator.creatorNickname}
                    </h3>
                    <p className="text-sm text-gray-500">
                      {creator.type === "PAID" ? "멤버십" : "일반 구독"}
                    </p>
                  </div>
                </Card>
              ))}
            </div>
          ) : (
            <div className="bg-gray-50 border border-gray-200 rounded-xl p-12 text-center">
              <div className="mb-4">
                <svg
                  className="w-16 h-16 mx-auto text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.5}
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                아직 구독한 크리에이터가 없습니다
              </h3>
              <p className="text-gray-500">
                관심 있는 크리에이터를 찾아 구독해보세요
              </p>
              {/* TODO: 크리에이터 탐색 기능 구현 시 아래 버튼 활성화 */}
              {/* <button
                onClick={() => router.push('/creators')}
                className="text-[#FF9500] hover:text-[#FFB800] font-semibold text-sm transition-colors mt-4"
              >
                크리에이터 둘러보기 →
              </button> */}
            </div>
          )}
        </div>
      )}

      {/* 검색 영역 */}
      <div className="mb-12">
        <Input
          type="text"
          placeholder="크리에이터 검색..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        {searchKeyword.trim() && (
          <p className="text-sm text-gray-500 mt-3">
            &quot;{searchKeyword}&quot; 검색 결과: {filteredCreators.length}개
          </p>
        )}
      </div>

      {/* 전체 크리에이터 목록 */}
      <div>
        <h2 className="text-sm font-semibold text-gray-600 mb-6 uppercase tracking-wider">
          {searchKeyword.trim() ? "검색 결과" : "전체 크리에이터"}
        </h2>
        {filteredCreators.length > 0 ? (
          <>
            <div className="grid gap-4">
              {filteredCreators.map((creator) => (
                <Card
                  key={creator.creatorId}
                  onClick={() => handleCreatorClick(creator.creatorId)}
                  className="flex items-center gap-6"
                >
                  {/* 프로필 */}
                  <CreatorProfileImage
                    creatorId={creator.creatorId}
                    nickname={creator.nickname}
                    size="sm"
                  />
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {creator.nickname}
                    </h3>
                  </div>
                </Card>
              ))}
            </div>
            {!searchKeyword.trim() && creators && (
              <div className="mt-12">
                <Pagination
                  currentPage={currentPage}
                  totalPages={creators.totalPages}
                  onPageChange={setCurrentPage}
                />
              </div>
            )}
          </>
        ) : (
          <div className="bg-gray-50 border border-gray-200 rounded-xl p-12 text-center">
            <p className="text-gray-500">
              {searchKeyword.trim()
                ? "검색 결과가 없습니다."
                : "크리에이터가 없습니다."}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
