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
    <div className="max-w-7xl mx-auto px-6 py-12">
      {/* 내가 구독한 크리에이터 (로그인 시, 검색 중이 아닐 때만) */}
      {!searchKeyword.trim() &&
        isLoggedIn &&
        subscribedCreators &&
        subscribedCreators.content.length > 0 && (
          <div className="mb-16 animate-slide-in">
            <div className="mb-8">
              <h2 className="text-4xl font-black gradient-text mb-3 neon-text">
                ⭐ 내가 구독한 크리에이터
              </h2>
              <p className="text-gray-400 text-lg">
                구독 중인 크리에이터들을 만나보세요
              </p>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
              {subscribedCreators.content.map((creator, index) => (
                <div
                  key={creator.creatorId}
                  onClick={() => handleCreatorClick(creator.creatorId)}
                  className="group cursor-pointer animate-fade-in-scale"
                  style={{animationDelay: `${index * 0.05}s`}}
                >
                  <div className="relative aspect-square overflow-hidden rounded-2xl border border-purple-500/20 hover:border-purple-500/60 transition-all duration-400 transform hover:scale-105 hover:rotate-1">
                    <CreatorProfileImage
                      creatorId={creator.creatorId}
                      nickname={creator.creatorNickname}
                      size="full"
                    />
                    {/* 그라데이션 오버레이 - 항상 표시 */}
                    <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent"></div>
                    {/* 호버 시 네온 그로우 효과 */}
                    <div className="absolute inset-0 bg-gradient-to-t from-purple-900/40 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-400"></div>
                    {/* 호버 시 외곽 글로우 */}
                    <div className="absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-400" style={{boxShadow: '0 0 30px rgba(131, 56, 236, 0.6), 0 0 60px rgba(255, 0, 110, 0.4)'}}></div>
                    {/* 닉네임과 배지 */}
                    <div className="absolute bottom-0 left-0 right-0 p-3">
                      <p className="text-white font-black text-xs mb-1.5 drop-shadow-lg truncate">
                        {creator.creatorNickname}
                      </p>
                      <div className="flex items-center opacity-0 group-hover:opacity-100 transition-all duration-300 transform translate-y-2 group-hover:translate-y-0">
                        {creator.type === "PAID" ? (
                          <span className="text-xs bg-gradient-to-r from-purple-600 to-pink-600 text-white px-2 py-1 rounded-full font-bold neon-glow">
                            💎 멤버십
                          </span>
                        ) : (
                          <span className="text-xs bg-gradient-to-r from-blue-600 to-cyan-600 text-white px-2 py-1 rounded-full font-bold shadow-lg shadow-cyan-500/50">
                            📌 구독중
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

      {/* 검색 영역 */}
      <div className="mb-12 max-w-2xl mx-auto animate-fade-in-scale">
        <Input
          type="text"
          placeholder="🔍 크리에이터 검색..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        {searchKeyword.trim() && (
          <p className="text-sm text-gray-400 mt-3 font-bold text-center animate-pulse">
            &quot;{searchKeyword}&quot; 검색 결과: {filteredCreators.length}개
          </p>
        )}
      </div>

      {/* 전체 크리에이터 목록 */}
      <div>
        <div className="mb-12 text-center animate-slide-in">
          <h2 className="text-5xl font-black gradient-text mb-4 neon-text">
            {searchKeyword.trim() ? "🔎 검색 결과" : "🎨 Our Creators"}
          </h2>
          <p className="text-gray-400 text-xl">
            {searchKeyword.trim() 
              ? "검색하신 크리에이터 목록입니다" 
              : "다양한 크리에이터들을 만나보세요"}
          </p>
        </div>
        {filteredCreators.length > 0 ? (
          <>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
              {filteredCreators.map((creator, index) => (
                <div
                  key={creator.creatorId}
                  onClick={() => handleCreatorClick(creator.creatorId)}
                  className="group cursor-pointer animate-fade-in-scale"
                  style={{animationDelay: `${index * 0.05}s`}}
                >
                  <div className="relative aspect-square overflow-hidden rounded-2xl border border-purple-500/20 hover:border-pink-500/60 transition-all duration-400 transform hover:scale-105 hover:-rotate-1">
                    <CreatorProfileImage
                      creatorId={creator.creatorId}
                      nickname={creator.nickname}
                      size="full"
                    />
                    {/* 그라데이션 오버레이 - 항상 표시 */}
                    <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent"></div>
                    {/* 호버 시 네온 효과 */}
                    <div className="absolute inset-0 bg-gradient-to-t from-pink-900/40 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-400"></div>
                    {/* 호버 시 외곽 글로우 */}
                    <div className="absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-400" style={{boxShadow: '0 0 30px rgba(255, 0, 110, 0.6), 0 0 60px rgba(131, 56, 236, 0.4)'}}></div>
                    {/* 닉네임 */}
                    <div className="absolute bottom-0 left-0 right-0 p-3">
                      <p className="text-white font-black text-xs drop-shadow-lg truncate group-hover:scale-110 transition-transform duration-300">
                        {creator.nickname}
                      </p>
                    </div>
                  </div>
                </div>
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
          <div className="glass p-12 text-center rounded-2xl max-w-md mx-auto border border-purple-500/20 animate-fade-in-scale">
            <div className="text-7xl mb-6 animate-pulse">🔍</div>
            <p className="text-gray-400 text-xl font-bold">
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
