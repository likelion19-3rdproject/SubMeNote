"use client";

import { useState } from "react";
import Image from "next/image";
import { profileImageApi } from "@/src/api/profileImageApi";

interface CreatorProfileImageProps {
  creatorId: number;
  nickname: string;
  size?: "sm" | "md" | "lg" | "xl" | "full";
}

export default function CreatorProfileImage({
  creatorId,
  nickname,
  size = "md",
}: CreatorProfileImageProps) {
  const [imageError, setImageError] = useState(false);
  const profileImageUrl = profileImageApi.getProfileImageUrl(creatorId);

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
        className={`${sizeClass} bg-gradient-to-br from-purple-600 via-pink-600 to-orange-600 flex items-center justify-center flex-shrink-0 animate-pulse-glow`}
      >
        <span className="font-black text-white drop-shadow-lg">
          {nickname ? nickname.charAt(0).toUpperCase() : "?"}
        </span>
      </div>
    );
  }

  return (
    <div
      className={`${sizeClass} overflow-hidden relative bg-gradient-to-br from-gray-800 to-gray-900 flex-shrink-0 ring-2 ring-purple-500/30`}
    >
      <Image
        src={profileImageUrl}
        alt={`${nickname} 프로필`}
        fill
        className="object-cover"
        onError={() => setImageError(true)}
        unoptimized
      />
    </div>
  );
}
