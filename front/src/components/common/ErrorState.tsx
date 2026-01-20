interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export default function ErrorState({ message = '오류가 발생했습니다.', onRetry }: ErrorStateProps) {
  return (
    <div className="glass p-8 rounded-2xl flex flex-col items-center justify-center border border-red-500/30 animate-fade-in-scale">
      <div className="text-6xl mb-4 animate-pulse">⚠️</div>
      <p className="text-red-400 font-bold mb-6 text-center text-lg">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="btn-interactive px-6 py-3 bg-gradient-to-r from-purple-500 to-purple-600 text-white rounded-xl hover:from-purple-400 hover:to-purple-500 neon-glow font-bold transform hover:scale-105 transition-all duration-300"
        >
          <span className="relative z-10">🔄 다시 시도</span>
        </button>
      )}
    </div>
  );
}

