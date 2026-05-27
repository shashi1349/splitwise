import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  listSettlements,
  recordSettlement,
  suggestSettlements,
  type TransferDto,
} from '@/api/settlements';
import { getProblemDetail } from '@/api/client';
import { centsToDecimalString, formatMoney } from '@/lib/currency';
import { formatRelative } from '@/lib/dates';
import Button from '@/components/ui/Button';
import EmptyState from '@/components/EmptyState';
import Skeleton from '@/components/Skeleton';
import Spinner from '@/components/ui/Spinner';
import { useGroupContext } from '../groups/useGroupContext';

interface TransferRowProps {
  transfer: TransferDto;
  groupId: number;
  onPaid: () => void;
}

function TransferRow({ transfer, groupId, onPaid }: TransferRowProps) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () =>
      recordSettlement(groupId, {
        fromUserId: transfer.fromUserId,
        toUserId: transfer.toUserId,
        amount: centsToDecimalString(transfer.amountCents),
        note: 'Marked as paid via Splitwise-Lite',
      }),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['balances', groupId] }),
        queryClient.invalidateQueries({ queryKey: ['settle', groupId] }),
        queryClient.invalidateQueries({ queryKey: ['settlements', groupId] }),
      ]);
      onPaid();
    },
    onError: (err) =>
      setError(getProblemDetail(err).detail ?? 'Could not record the settlement.'),
  });

  return (
    <li className="rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm">
          <span className="font-medium text-slate-900 dark:text-slate-100">
            {transfer.fromUserDisplayName}
          </span>{' '}
          <span aria-hidden className="text-slate-400">
            &rarr;
          </span>{' '}
          <span className="font-medium text-slate-900 dark:text-slate-100">
            {transfer.toUserDisplayName}
          </span>
          <span className="ml-2 text-slate-500">
            : {formatMoney(transfer.amountCents, transfer.currencyCode)}
          </span>
        </p>
        <Button
          size="sm"
          onClick={() => {
            setError(null);
            mutation.mutate();
          }}
          disabled={mutation.isPending}
        >
          {mutation.isPending ? (
            <>
              <Spinner className="mr-2" />
              Saving
            </>
          ) : (
            'Mark as paid'
          )}
        </Button>
      </div>
      {error && (
        <p
          role="alert"
          className="mt-2 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
        >
          {error}
        </p>
      )}
    </li>
  );
}

export default function SettleUpTab() {
  const { group, refetch } = useGroupContext();

  const suggestions = useQuery({
    queryKey: ['settle', group.id],
    queryFn: () => suggestSettlements(group.id),
  });

  const history = useQuery({
    queryKey: ['settlements', group.id],
    queryFn: () => listSettlements(group.id),
  });

  return (
    <div className="space-y-8">
      <section className="space-y-3">
        <header>
          <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">
            Suggested transfers
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            The fewest moves that settle every balance — at most one less
            transfer than there are people with non-zero balances.
          </p>
        </header>

        {suggestions.isLoading && (
          <div className="space-y-2">
            {[0, 1].map((i) => (
              <Skeleton key={i} className="h-14 w-full" />
            ))}
          </div>
        )}

        {suggestions.error && (
          <p
            role="alert"
            className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
          >
            Could not load suggestions:{' '}
            {getProblemDetail(suggestions.error).detail ?? 'unknown error'}
          </p>
        )}

        {suggestions.data && suggestions.data.length === 0 && (
          <EmptyState
            title="Everyone is settled up"
            description="Add an expense to start tracking again."
          />
        )}

        {suggestions.data && suggestions.data.length > 0 && (
          <ul className="space-y-2">
            {suggestions.data.map((t, idx) => (
              <TransferRow
                key={`${t.fromUserId}-${t.toUserId}-${idx}`}
                transfer={t}
                groupId={group.id}
                onPaid={() => refetch()}
              />
            ))}
          </ul>
        )}
      </section>

      {history.data && history.data.length > 0 && (
        <section className="space-y-3">
          <header>
            <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">
              Settlement history
            </h2>
            <p className="text-sm text-slate-500 dark:text-slate-400">
              Transfers already recorded.
            </p>
          </header>
          <ul className="divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white dark:divide-slate-800 dark:border-slate-800 dark:bg-slate-900">
            {history.data.map((s) => (
              <li
                key={s.id}
                className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 text-sm"
              >
                <div className="min-w-0">
                  <p className="text-slate-900 dark:text-slate-100">
                    <span className="font-medium">{s.fromUserDisplayName}</span>{' '}
                    <span aria-hidden className="text-slate-400">
                      &rarr;
                    </span>{' '}
                    <span className="font-medium">{s.toUserDisplayName}</span>
                    <span className="ml-2 text-slate-500">
                      : {formatMoney(s.amountCents, s.currencyCode)}
                    </span>
                  </p>
                  {s.note && (
                    <p className="truncate text-xs text-slate-500 dark:text-slate-400">
                      {s.note}
                    </p>
                  )}
                </div>
                <time
                  dateTime={s.settledAt}
                  className="text-xs text-slate-500 dark:text-slate-400"
                >
                  {formatRelative(s.settledAt)}
                </time>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
