interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export default function ErrorState({ message = '오류가 발생했습니다.', onRetry }: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16">
      <div className="bg-red-50 border border-red-200 rounded-xl p-6 max-w-md w-full text-center">
        <p className="text-red-600 mb-6 font-medium">{message}</p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="px-6 py-3 bg-red-600 text-white rounded-xl hover:bg-red-700 font-semibold transition-all duration-200 shadow-sm hover:shadow"
          >
            다시 시도
          </button>
        )}
      </div>
    </div>
  );
}

