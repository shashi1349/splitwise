import { useState } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { listExpenses } from '@/api/expenses';
import { getProblemDetail } from '@/api/client';
import Button from '@/components/ui/Button';
import EmptyState from '@/components/EmptyState';
import Skeleton from '@/components/Skeleton';
import { useGroupContext } from '../groups/useGroupContext';
import ExpenseCard from './ExpenseCard';
import AddExpenseDialog from './AddExpenseDialog';

const PAGE_SIZE = 20;

export default function ExpensesTab() {
  const { group } = useGroupContext();
  const [addOpen, setAddOpen] = useState(false);
  const [page, setPage] = useState(0);

  const { data, isLoading, isFetching, error } = useQuery({
    queryKey: ['expenses', group.id, page],
    queryFn: () => listExpenses(group.id, page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  });

  return (
    <div className="space-y-4">
      <header className="flex items-center justify-between gap-3">
        <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">
          Expenses
          {data && (
            <span className="ml-2 text-sm font-normal text-slate-500">
              ({data.totalElements})
            </span>
          )}
        </h2>
        <Button onClick={() => setAddOpen(true)}>Add expense</Button>
      </header>

      {isLoading && (
        <div className="space-y-3">
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} className="h-20 w-full" />
          ))}
        </div>
      )}

      {error && (
        <p
          role="alert"
          className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
        >
          Could not load expenses: {getProblemDetail(error).detail ?? 'unknown error'}
        </p>
      )}

      {data && data.content.length === 0 && (
        <EmptyState
          title="No expenses yet"
          description="Track your first shared expense to see balances update in real time."
          action={<Button onClick={() => setAddOpen(true)}>Add an expense</Button>}
        />
      )}

      {data && data.content.length > 0 && (
        <ul className="space-y-3">
          {data.content.map((e) => (
            <li key={e.id}>
              <ExpenseCard expense={e} />
            </li>
          ))}
        </ul>
      )}

      {data && data.totalPages > 1 && (
        <nav
          aria-label="Pagination"
          className="flex items-center justify-between gap-3 pt-2"
        >
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Page {data.page + 1} of {data.totalPages}
          </p>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={data.page === 0 || isFetching}
            >
              Previous
            </Button>
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setPage((p) => p + 1)}
              disabled={!data.hasNext || isFetching}
            >
              Next
            </Button>
          </div>
        </nav>
      )}

      <AddExpenseDialog
        open={addOpen}
        onClose={() => setAddOpen(false)}
        group={group}
      />
    </div>
  );
}
