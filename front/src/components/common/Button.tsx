import { ReactNode, ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
}

export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  className = '',
  ...props
}: ButtonProps) {
  const baseClasses = 'font-bold rounded-xl transition-all duration-300 transform active:scale-95 disabled:opacity-40 disabled:cursor-not-allowed relative overflow-hidden btn-interactive';
  
  const variantClasses = {
    primary: 'bg-gradient-to-r from-purple-600 to-pink-600 text-white hover:from-purple-500 hover:to-pink-500 neon-glow hover:scale-105',
    secondary: 'glass text-gray-200 hover:text-white border border-purple-500/30 hover:border-purple-500/60 hover:bg-white/10',
    danger: 'bg-gradient-to-r from-red-600 to-pink-600 text-white hover:from-red-500 hover:to-pink-500 shadow-lg shadow-red-500/30 hover:shadow-red-500/50 hover:scale-105',
  };

  const sizeClasses = {
    sm: 'px-4 py-2 text-sm',
    md: 'px-6 py-2.5 text-sm',
    lg: 'px-8 py-3 text-base',
  };

  return (
    <button
      className={`${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]} ${className}`}
      {...props}
    >
      <span className="relative z-10">{children}</span>
    </button>
  );
}

