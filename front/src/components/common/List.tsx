import { ReactNode } from 'react';

interface ListProps {
  children: ReactNode;
  className?: string;
}

export default function List({ children, className = '' }: ListProps) {
  return <div className={`space-y-4 ${className}`}>{children}</div>;
}

