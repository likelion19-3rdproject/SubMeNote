import { ReactNode } from "react";

interface CardProps {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
}

export default function Card({ children, className = "", onClick }: CardProps) {
  return (
    <div
      className={`bg-white rounded-xl border border-gray-200 shadow-sm p-6 ${
        onClick ? "cursor-pointer hover:shadow-md hover:border-gray-300 transition-all duration-200" : ""
      } ${className}`}
      onClick={onClick}
    >
      {children}
    </div>
  );
}
