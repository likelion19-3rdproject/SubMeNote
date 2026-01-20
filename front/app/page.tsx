"use client";

import { useEffect, useState, useCallback, useRef } from "react";
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
    useState<SubscribedCreatorResponseDto[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [scrollPosition, setScrollPosition] = useState(0);
  const sliderRef = useRef<HTMLDivElement>(null);
  const [centerIndex, setCenterIndex] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [startX, setStartX] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);
  const isScrollingRef = useRef(false);
  const scrollTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const centerIndexRef = useRef(0);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // 크리에이터 목록 로드
      const creatorsData = await homeApi.getCreators(currentPage, 10);

      // 로그인 상태 확인 (구독 목록 전체 로드)
      let allSubscribedCreators: SubscribedCreatorResponseDto[] = [];
      try {
        let page = 0;
        let hasMore = true;
        while (hasMore) {
          const subscribedData = await subscribeApi.getMyCreators(page, 100);
          allSubscribedCreators = [...allSubscribedCreators, ...subscribedData.content];
          hasMore = subscribedData.content.length === 100 && page < subscribedData.totalPages - 1;
          page++;
        }
        setIsLoggedIn(true);
      } catch (err) {
        // 인증되지 않은 경우 무시
        setIsLoggedIn(false);
      }

      setCreators(creatorsData);
      setSubscribedCreators(allSubscribedCreators);
    } catch (err: any) {
      setError(err.message || "데이터를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // 구독한 크리에이터 드래그 핸들러
  const handleMouseDown = (e: React.MouseEvent) => {
    if (!sliderRef.current) return;
    setIsDragging(true);
    setStartX(e.pageX - sliderRef.current.offsetLeft);
    setScrollLeft(sliderRef.current.scrollLeft);
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging || !sliderRef.current) return;
    e.preventDefault();
    const x = e.pageX - sliderRef.current.offsetLeft;
    const walk = (x - startX) * 2;
    sliderRef.current.scrollLeft = scrollLeft - walk;
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleMouseLeave = () => {
    setIsDragging(false);
  };

  // 구독한 크리에이터 클릭 핸들러 (중앙으로 이동 후 페이지 이동)
  const handleSubscribedCreatorClick = (creatorId: number, index: number) => {
    if (!sliderRef.current) return;
    const container = sliderRef.current;
    const containerWidth = container.clientWidth;
    
    // 왼쪽 여유 공간 너비 계산
    const leftSpacerWidth = typeof window !== 'undefined' ? Math.max(window.innerWidth / 2 - 150, 200) : 200;
    
    // 클릭한 항목까지의 위치 계산 (왼쪽 여유 공간 고려)
    let currentPosition = leftSpacerWidth;
    for (let i = 0; i < index; i++) {
      currentPosition += 200 + 24; // 일반 항목 크기 + gap
    }
    
    // 클릭한 항목의 중앙을 화면 중앙에 맞춤
    const clickedItemSize = 300; // 확대된 항목 크기
    const targetScroll = currentPosition + clickedItemSize / 2 - containerWidth / 2;
    
    // 최대 스크롤 가능한 위치 계산
    const maxScroll = Math.max(0, container.scrollWidth - containerWidth);
    
    // 스크롤 위치를 최대 범위 내로 제한
    const finalScroll = Math.min(maxScroll, Math.max(0, targetScroll));
    
    container.scrollTo({
      left: finalScroll,
      behavior: 'smooth'
    });
    
    // 중앙 인덱스 업데이트
    setCenterIndex(index);
    
    setTimeout(() => {
      handleCreatorClick(creatorId);
    }, 300);
  };

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

  // 구독한 크리에이터 스크롤 위치 업데이트 및 중앙 인덱스 계산
  useEffect(() => {
    const container = sliderRef.current;
    if (container && subscribedCreators.length > 0) {
      const handleScroll = () => {
        // 프로그래밍적으로 스크롤 중이면 무시
        if (isScrollingRef.current) return;
        
        const scrollLeft = container.scrollLeft;
        setScrollPosition(scrollLeft);
        
        // 중앙 인덱스 계산 - 정확히 화면 중앙에 오도록
        const containerWidth = container.clientWidth;
        const centerPosition = scrollLeft + containerWidth / 2;
        
        // 각 항목의 실제 위치 계산 (왼쪽 여유 공간 고려)
        // 모든 항목을 200px로 계산하되, 중앙 항목만 시각적으로 300px로 확대
        const leftSpacerWidth = typeof window !== 'undefined' ? Math.max(window.innerWidth / 2 - 150, 200) : 200;
        let currentPosition = leftSpacerWidth;
        let newCenterIndex = 0;
        let minDistance = Infinity;
        
        for (let i = 0; i < subscribedCreators.length; i++) {
          // 모든 항목은 기본 크기(200px)로 계산
          const itemSize = 200;
          const itemCenter = currentPosition + itemSize / 2;
          const distance = Math.abs(centerPosition - itemCenter);
          
          if (distance < minDistance) {
            minDistance = distance;
            newCenterIndex = i;
          }
          
          // 다음 항목 위치 계산
          currentPosition += itemSize + 24; // gap 포함
        }
        
        // 새로운 중앙 인덱스가 변경되었을 때만 업데이트 (즉시 실행)
        const prevIndex = centerIndexRef.current;
        if (newCenterIndex !== prevIndex) {
          centerIndexRef.current = newCenterIndex;
          setCenterIndex(newCenterIndex);
        }
      };
      
      container.addEventListener('scroll', handleScroll, { passive: true });
      handleScroll(); // 초기 계산
      
      return () => {
        container.removeEventListener('scroll', handleScroll);
      };
    }
  }, [subscribedCreators]);

  // 중앙 인덱스가 변경되면 스크롤 위치 조정
  useEffect(() => {
    const container = sliderRef.current;
    if (container && subscribedCreators.length > 0 && centerIndex >= 0) {
      // centerIndexRef 업데이트
      centerIndexRef.current = centerIndex;
      
      // 프로그래밍적 스크롤 플래그 설정
      isScrollingRef.current = true;
      
      const containerWidth = container.clientWidth;
      
      // 왼쪽 여유 공간 너비 계산
      const leftSpacerWidth = typeof window !== 'undefined' ? Math.max(window.innerWidth / 2 - 150, 200) : 200;
      
      // 중앙 인덱스까지의 위치 계산 (왼쪽 여유 공간 + 모든 항목은 200px로 계산)
      let currentPosition = leftSpacerWidth;
      for (let i = 0; i < centerIndex; i++) {
        currentPosition += 200 + 24; // 일반 항목 크기 + gap
      }
      
      // 중앙 인덱스 항목의 중앙을 화면 중앙에 맞춤
      const centerItemSize = 300; // 확대된 항목 크기
      const targetScroll = currentPosition + centerItemSize / 2 - containerWidth / 2;
      
      // 최대 스크롤 가능한 위치 계산
      const maxScroll = Math.max(0, container.scrollWidth - containerWidth);
      
      // 스크롤 위치를 최대 범위 내로 제한하되, 첫 번째와 마지막 항목도 중앙에 올 수 있도록
      const finalScroll = Math.min(maxScroll, Math.max(0, targetScroll));
      
      container.scrollTo({
        left: finalScroll,
        behavior: 'smooth'
      });
      
      // 스크롤 완료 후 플래그 해제 (스크롤 애니메이션 시간 고려)
      if (scrollTimeoutRef.current) {
        clearTimeout(scrollTimeoutRef.current);
      }
      scrollTimeoutRef.current = setTimeout(() => {
        isScrollingRef.current = false;
      }, 600);
    }
  }, [centerIndex, subscribedCreators.length]);

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
        subscribedCreators.length > 0 && (
          <div className="mb-16 animate-slide-in">
            <div className="mb-8">
              <h2 className="text-4xl font-black text-white mb-3">
                <span>🎨</span> <span className="gradient-text">My Creator</span>
              </h2>
              <p className="text-gray-400 text-lg">
                구독 중인 크리에이터들을 만나보세요
              </p>
            </div>
            <div className="relative">
              {/* 좌측 화살표 버튼들 */}
              <div className="absolute left-0 top-1/2 -translate-y-1/2 z-10 flex gap-2">
                <button
                  onClick={() => {
                    if (sliderRef.current && centerIndex > 0) {
                      const newIndex = Math.max(0, centerIndex - 5);
                      setCenterIndex(newIndex);
                    }
                  }}
                  className="bg-black/50 hover:bg-black/70 text-white p-3 rounded-full transition-all duration-300 backdrop-blur-sm"
                  title="5개 이전"
                  disabled={centerIndex === 0}
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
                  </svg>
                </button>
                <button
                  onClick={() => {
                    if (sliderRef.current && centerIndex > 0) {
                      const newIndex = Math.max(0, centerIndex - 1);
                      setCenterIndex(newIndex);
                    }
                  }}
                  className="bg-black/50 hover:bg-black/70 text-white p-3 rounded-full transition-all duration-300 backdrop-blur-sm"
                  title="이전 크리에이터"
                  disabled={centerIndex === 0}
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                </button>
              </div>
              
              {/* 슬라이더 컨테이너 */}
              <div
                ref={sliderRef}
                className="flex items-center gap-6 overflow-x-auto scroll-smooth pb-4 [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none] cursor-grab active:cursor-grabbing select-none"
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseLeave}
              >
                {/* 첫 번째 항목도 중앙에 올 수 있도록 여유 공간 추가 */}
                <div 
                  className="flex-shrink-0" 
                  style={{ 
                    width: 'calc(50vw - 150px)', 
                    minWidth: '200px' 
                  }}
                />
                {subscribedCreators.map((creator, index) => {
                  const isCenter = index === centerIndex;
                  const scale = isCenter ? 1.5 : 1;
                  
                  return (
                    <div
                      key={creator.creatorId}
                      onClick={() => handleSubscribedCreatorClick(creator.creatorId, index)}
                      className="group flex-shrink-0 animate-fade-in-scale transition-transform duration-300 ease-out select-none"
                      style={{
                        animationDelay: `${index * 0.05}s`,
                        minWidth: isCenter ? '300px' : '200px',
                        maxWidth: isCenter ? '300px' : '200px',
                        transform: `scale(${scale})`,
                        transformOrigin: 'center center',
                        zIndex: isCenter ? 10 : 1,
                        willChange: 'transform',
                        userSelect: 'none',
                        WebkitUserSelect: 'none',
                        MozUserSelect: 'none',
                        msUserSelect: 'none'
                      }}
                    >
                      <div className={`relative aspect-square overflow-hidden rounded-2xl border transition-all duration-300 ${
                        isCenter 
                          ? 'border-purple-500/60 shadow-2xl shadow-purple-500/50' 
                          : 'border-purple-500/20 hover:border-purple-500/40'
                      } ${isDragging ? '' : 'transform hover:scale-105 hover:rotate-1'}`}>
                    <div className="w-full h-full">
                      <CreatorProfileImage
                        creatorId={creator.creatorId}
                        nickname={creator.creatorNickname}
                        size="full"
                      />
                    </div>
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
                          <span className="text-xs bg-gradient-to-r from-purple-500 to-purple-600 text-white px-2 py-1 rounded-full font-bold neon-glow">
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
                  );
                })}
                {/* 마지막 항목도 중앙에 올 수 있도록 여유 공간 추가 */}
                <div 
                  className="flex-shrink-0" 
                  style={{ 
                    width: 'calc(50vw - 150px)', 
                    minWidth: '200px' 
                  }}
                />
              </div>
              
              {/* 우측 화살표 버튼들 */}
              <div className="absolute right-0 top-1/2 -translate-y-1/2 z-10 flex gap-2">
                <button
                  onClick={() => {
                    if (sliderRef.current && centerIndex < subscribedCreators.length - 1) {
                      const newIndex = centerIndex + 1;
                      setCenterIndex(newIndex);
                    }
                  }}
                  className="bg-black/50 hover:bg-black/70 text-white p-3 rounded-full transition-all duration-300 backdrop-blur-sm"
                  title="다음 크리에이터"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </button>
                <button
                  onClick={() => {
                    if (sliderRef.current && centerIndex < subscribedCreators.length - 1) {
                      const newIndex = Math.min(centerIndex + 5, subscribedCreators.length - 1);
                      setCenterIndex(newIndex);
                    }
                  }}
                  className="bg-black/50 hover:bg-black/70 text-white p-3 rounded-full transition-all duration-300 backdrop-blur-sm"
                  title="5개 다음"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 5l7 7-7 7M5 5l7 7-7 7" />
                  </svg>
                </button>
              </div>
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
          <h2 className="text-5xl font-black text-white mb-4">
            {searchKeyword.trim() ? <span>🔎</span> : <span>🎨</span>} <span className="gradient-text">{searchKeyword.trim() ? "검색 결과" : "Our Creators"}</span>
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
                    <div className="absolute inset-0 bg-gradient-to-t from-purple-900/30 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-400"></div>
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
