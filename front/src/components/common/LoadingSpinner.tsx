export default function LoadingSpinner() {
  return (
    <div className="flex flex-col justify-center items-center py-12">
      <div className="relative">
        <div className="animate-spin rounded-full h-16 w-16 border-4 border-purple-500/20"></div>
        <div className="animate-spin rounded-full h-16 w-16 border-4 border-transparent border-t-purple-500 absolute top-0 left-0 neon-glow"></div>
        <div className="animate-spin rounded-full h-16 w-16 border-4 border-transparent border-t-pink-500 absolute top-0 left-0 animate-pulse" style={{animationDirection: 'reverse'}}></div>
      </div>
      <p className="mt-6 text-gray-300 font-bold text-lg animate-pulse">로딩 중...</p>
    </div>
  );
}

