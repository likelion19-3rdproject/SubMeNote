"use client";

import { useState } from "react";
import Image from "next/image";
import { profileImageApi } from "@/src/api/profileImageApi";

interface CreatorProfileImageProps {
  creatorId: number;
  nickname: string;
  size?: "sm" | "md" | "lg" | "xl";
}

export default function CreatorProfileImage({
  creatorId,
  nickname,
  size = "md",
}: CreatorProfileImageProps) {
  const [imageError, setImageError] = useState(false);
  const profileImageUrl = profileImageApi.getProfileImageUrl(creatorId);

  const sizeClasses = {
    sm: "w-16 h-16 text-xl",
    md: "w-24 h-24 text-4xl",
    lg: "w-32 h-32 text-5xl",
    xl: "w-48 h-48 text-6xl",
  };

  const sizeClass = sizeClasses[size];

  if (imageError) {
    return (
      <div
        className={`${sizeClass} rounded-full bg-gradient-to-br from-[#FFC837] to-[#FF9500] flex items-center justify-center flex-shrink-0 shadow-sm`}
      >
        <span className="font-bold text-white">
          {nickname ? nickname.charAt(0).toUpperCase() : "?"}
        </span>
      </div>
    );
  }

  return (
    <div
      className={`${sizeClass} rounded-full overflow-hidden relative bg-gray-200 flex-shrink-0`}
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
