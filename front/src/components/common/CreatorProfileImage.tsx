"use client";

import { useState, useMemo, memo } from "react";
import { profileImageApi } from "@/src/api/profileImageApi";

interface CreatorProfileImageProps {
  creatorId: number;
  nickname: string;
  size?: "sm" | "md" | "lg" | "xl" | "full";
}

function CreatorProfileImage({
  creatorId,
  nickname,
  size = "md",
}: CreatorProfileImageProps) {
  const [imageError, setImageError] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);
  
  // URL을 메모이제이션하여 불필요한 재생성 방지
  const profileImageUrl = useMemo(
    () => profileImageApi.getProfileImageUrl(creatorId),
    [creatorId]
  );

  const sizeClasses = {
    sm: "w-16 h-16 text-xl rounded-full",
    md: "w-24 h-24 text-4xl rounded-full",
    lg: "w-32 h-32 text-5xl rounded-full",
    xl: "w-48 h-48 text-6xl rounded-full",
    full: "w-full h-full text-7xl rounded-2xl",
  };

  const sizeClass = sizeClasses[size];

  if (imageError) {
    return (
      <div
        className={`${sizeClass} bg-gradient-to-br from-purple-500 via-purple-600 to-blue-500 flex items-center justify-center flex-shrink-0 animate-pulse-glow`}
      >
        <span className="font-black text-white drop-shadow-lg">
          {nickname ? nickname.charAt(0).toUpperCase() : "?"}
        </span>
      </div>
    );
  }

  return (
    <div
      className={`${sizeClass} overflow-hidden relative bg-gradient-to-br from-gray-800 to-gray-900 flex-shrink-0 ring-2 ring-purple-400/25`}
    >
      {!imageLoaded && (
        <div className="absolute inset-0 bg-gradient-to-br from-gray-800 to-gray-900 flex items-center justify-center">
          <span className="font-black text-white/50 text-2xl">
            {nickname ? nickname.charAt(0).toUpperCase() : "?"}
          </span>
        </div>
      )}
      <img
        src={profileImageUrl}
        alt={`${nickname} 프로필`}
        className="absolute inset-0 w-full h-full object-cover"
        onError={() => setImageError(true)}
        onLoad={() => setImageLoaded(true)}
        loading="lazy"
        decoding="async"
        style={{ 
          imageRendering: 'auto'
        }}
      />
    </div>
  );
}

// React.memo로 컴포넌트 메모이제이션하여 불필요한 재렌더링 방지
export default memo(CreatorProfileImage);
