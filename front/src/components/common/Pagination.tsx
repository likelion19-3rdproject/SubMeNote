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
    <div className="flex justify-center items-center space-x-1 mt-12">
      <button
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
        disabled={currentPage === 0}
        className="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors font-normal"
      >
        이전
      </button>

      {pages.map((page) => (
        <button
          key={page}
          onClick={() => onPageChange(page)}
          className={`px-4 py-2 text-sm border rounded-sm transition-colors font-normal ${
            currentPage === page
              ? 'bg-gray-900 text-white border-gray-900'
              : 'text-gray-600 border-gray-200 hover:bg-gray-50'
          }`}
        >
          {page + 1}
        </button>
      ))}

      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
        disabled={currentPage >= totalPages - 1}
        className="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors font-normal"
      >
        다음
      </button>
    </div>
  );
}

