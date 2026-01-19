import { InputHTMLAttributes, forwardRef } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, className = '', ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm font-bold text-gray-300 mb-2">
            {label}
          </label>
        )}
        <input
          ref={ref}
          className={`w-full px-4 py-3 glass border-2 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500/60 transition-all duration-300 text-white placeholder-gray-500 hover:border-purple-500/40 ${
            error ? 'border-red-500/50 focus:ring-red-500 focus:border-red-500/60' : 'border-purple-500/20'
          } ${className}`}
          {...props}
        />
        {error && <p className="mt-2 text-sm text-red-400 font-medium">{error}</p>}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;

