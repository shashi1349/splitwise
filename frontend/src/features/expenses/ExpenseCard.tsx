import type { ExpenseDto } from '@/api/expenses';
import { formatMoney } from '@/lib/currency';
import { formatRelative } from '@/lib/dates';

interface ExpenseCardProps {
  expense: ExpenseDto;
}

const SPLIT_LABEL: Record<ExpenseDto['splitType'], string> = {
  EQUAL: 'split equally',
  EXACT: 'split by exact amounts',
  PERCENT: 'split by percentage',
};

export default function ExpenseCard({ expense }: ExpenseCardProps) {
  const shareSummary = expense.shares
    .map((s) => `${s.displayName} ${formatMoney(s.shareCents, expense.currencyCode)}`)
    .join(' · ');

  return (
    <article className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <header className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="truncate text-base font-semibold text-slate-900 dark:text-slate-100">
            {expense.description}
          </h3>
          <p className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
            {expense.payerDisplayName} paid · {SPLIT_LABEL[expense.splitType]} ·{' '}
            <time dateTime={expense.occurredAt}>{formatRelative(expense.occurredAt)}</time>
          </p>
        </div>
        <span className="whitespace-nowrap text-base font-semibold text-slate-900 dark:text-slate-100">
          {formatMoney(expense.amountCents, expense.currencyCode)}
        </span>
      </header>
      <p className="mt-3 break-words text-xs text-slate-600 dark:text-slate-400">
        {shareSummary}
      </p>
    </article>
  );
}
