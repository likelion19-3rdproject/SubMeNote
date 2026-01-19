'use client';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  const pages = Array.from({ length: totalPages }, (_, i) => i);

  if (totalPages <= 1) return null;

  return (
    <div className="flex justify-center items-center gap-3 mt-12 animate-fade-in-scale">
      <button
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
        disabled={currentPage === 0}
        className="px-6 py-3 text-sm font-bold glass text-gray-300 border-2 border-purple-500/30 rounded-xl disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 hover:border-purple-500/60 hover:text-white transition-all duration-300 btn-interactive"
      >
        ← 이전
      </button>

      <div className="flex gap-2">
        {pages.map((page) => (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={`w-12 h-12 text-sm font-black rounded-xl transition-all duration-300 ${
              currentPage === page
                ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white neon-glow scale-110'
                : 'glass text-gray-400 border-2 border-purple-500/20 hover:bg-white/10 hover:border-purple-500/60 hover:text-white hover:scale-105'
            }`}
          >
            {page + 1}
          </button>
        ))}
      </div>

      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
        disabled={currentPage >= totalPages - 1}
        className="px-6 py-3 text-sm font-bold glass text-gray-300 border-2 border-purple-500/30 rounded-xl disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 hover:border-purple-500/60 hover:text-white transition-all duration-300 btn-interactive"
      >
        다음 →
      </button>
    </div>
  );
}

