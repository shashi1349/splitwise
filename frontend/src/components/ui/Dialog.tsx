import {
  Dialog as HUIDialog,
  DialogBackdrop,
  DialogPanel,
  DialogTitle,
} from '@headlessui/react';
import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

interface DialogProps {
  open: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children: ReactNode;
  className?: string;
}

/**
 * Accessible modal dialog built on Headless UI. Handles focus trapping,
 * Escape-to-close, and an a11y-correct `aria-labelledby` automatically.
 */
export default function Dialog({
  open,
  onClose,
  title,
  description,
  children,
  className,
}: DialogProps) {
  return (
    <HUIDialog open={open} onClose={onClose} className="relative z-50">
      <DialogBackdrop className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm" />
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <DialogPanel
          className={cn(
            'w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-xl dark:border-slate-800 dark:bg-slate-900',
            className,
          )}
        >
          <DialogTitle className="text-lg font-semibold text-slate-900 dark:text-slate-100">
            {title}
          </DialogTitle>
          {description && (
            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              {description}
            </p>
          )}
          <div className="mt-4">{children}</div>
        </DialogPanel>
      </div>
    </HUIDialog>
  );
}
