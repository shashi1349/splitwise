import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@/lib/cn';

interface FormFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string | undefined;
  hint?: string | undefined;
}

const FormField = forwardRef<HTMLInputElement, FormFieldProps>(
  ({ label, error, hint, id, className, ...rest }, ref) => {
    const describedBy: string[] = [];
    if (hint) describedBy.push(`${id}-hint`);
    if (error) describedBy.push(`${id}-error`);

    return (
      <div className="flex flex-col gap-1.5">
        <label
          htmlFor={id}
          className="text-sm font-medium text-slate-700 dark:text-slate-200"
        >
          {label}
        </label>
        <input
          id={id}
          ref={ref}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={describedBy.length ? describedBy.join(' ') : undefined}
          className={cn(
            'rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm placeholder:text-slate-400',
            'focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/30',
            'dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:placeholder:text-slate-500',
            error &&
              'border-red-400 focus:border-red-500 focus:ring-red-500/30 dark:border-red-500',
            className,
          )}
          {...rest}
        />
        {hint && (
          <p id={`${id}-hint`} className="text-xs text-slate-500">
            {hint}
          </p>
        )}
        {error && (
          <p
            id={`${id}-error`}
            role="alert"
            className="text-xs text-red-600 dark:text-red-400"
          >
            {error}
          </p>
        )}
      </div>
    );
  },
);
FormField.displayName = 'FormField';

export default FormField;
