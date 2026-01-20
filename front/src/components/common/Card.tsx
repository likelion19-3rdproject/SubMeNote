import { ReactNode } from "react";

interface CardProps {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
}

export default function Card({ children, className = "", onClick }: CardProps) {
  return (
    <div
      className={`glass rounded-2xl border border-purple-400/20 p-6 mb-4 transition-all duration-400 hover:border-purple-400/35 ${
        onClick ? "cursor-pointer card-hover group" : ""
      } ${className}`}
      onClick={onClick}
      style={{
        background: 'linear-gradient(135deg, rgba(31, 31, 46, 0.9) 0%, rgba(31, 31, 46, 0.75) 100%)'
      }}
    >
      {children}
    </div>
  );
}
