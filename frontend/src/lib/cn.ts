import clsx, { type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Tailwind-aware class merger. Combines clsx semantics (truthy filter,
 * arrays, objects) with tailwind-merge to dedupe conflicting classes.
 */
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}
