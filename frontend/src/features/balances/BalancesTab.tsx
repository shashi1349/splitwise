import { useQuery } from '@tanstack/react-query';
import { listBalances, type BalanceDto } from '@/api/balances';
import { getProblemDetail } from '@/api/client';
import { formatMoney } from '@/lib/currency';
import EmptyState from '@/components/EmptyState';
import Skeleton from '@/components/Skeleton';
import { useGroupContext } from '../groups/useGroupContext';
import { cn } from '@/lib/cn';

function describe(b: BalanceDto): { kind: 'creditor' | 'debtor' | 'settled'; line: string } {
  if (b.netCents > 0) {
    return {
      kind: 'creditor',
      line: `${b.displayName} is owed ${formatMoney(b.netCents, b.currencyCode)}`,
    };
  }
  if (b.netCents < 0) {
    return {
      kind: 'debtor',
      line: `${b.displayName} owes ${formatMoney(-b.netCents, b.currencyCode)}`,
    };
  }
  return { kind: 'settled', line: `${b.displayName} is settled up` };
}

interface BalanceRowProps {
  balance: BalanceDto;
  scale: number;
}

function BalanceRow({ balance, scale }: BalanceRowProps) {
  const { kind, line } = describe(balance);
  // Width as 0..50% of the bar, since we render a centered axis.
  const widthPct = scale === 0 ? 0 : (Math.abs(balance.netCents) / scale) * 50;

  return (
    <li className="grid grid-cols-1 gap-2 rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900 sm:grid-cols-[minmax(120px,1fr),2fr,minmax(180px,1fr)] sm:items-center">
      <p className="font-medium text-slate-900 dark:text-slate-100">
        {balance.displayName}
      </p>
      <div
        className="relative h-3 rounded bg-slate-100 dark:bg-slate-800"
        role="img"
        aria-label={line}
      >
        <div className="absolute inset-y-0 left-1/2 w-px bg-slate-400 dark:bg-slate-600" />
        {kind === 'creditor' && (
          <div
            className="absolute inset-y-0 left-1/2 rounded-r bg-emerald-500 dark:bg-emerald-600"
            style={{ width: `${widthPct}%` }}
          />
        )}
        {kind === 'debtor' && (
          <div
            className="absolute inset-y-0 right-1/2 rounded-l bg-red-500 dark:bg-red-600"
            style={{ width: `${widthPct}%` }}
          />
        )}
      </div>
      <p
        className={cn(
          'text-sm sm:text-right',
          kind === 'creditor' && 'text-emerald-700 dark:text-emerald-400',
          kind === 'debtor' && 'text-red-700 dark:text-red-400',
          kind === 'settled' && 'text-slate-500 dark:text-slate-400',
        )}
      >
        {line}
      </p>
    </li>
  );
}

export default function BalancesTab() {
  const { group } = useGroupContext();
  const { data, isLoading, error } = useQuery({
    queryKey: ['balances', group.id],
    queryFn: () => listBalances(group.id),
  });

  if (isLoading) {
    return (
      <div className="space-y-3">
        {[0, 1, 2].map((i) => (
          <Skeleton key={i} className="h-16 w-full" />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <p
        role="alert"
        className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
      >
        Could not load balances: {getProblemDetail(error).detail ?? 'unknown error'}
      </p>
    );
  }

  if (!data || data.length === 0) {
    return (
      <EmptyState
        title="No balances yet"
        description="Add an expense to see who owes whom."
      />
    );
  }

  const allSettled = data.every((b) => b.netCents === 0);
  const scale = data.reduce((acc, b) => Math.max(acc, Math.abs(b.netCents)), 0);

  return (
    <div className="space-y-4">
      <header>
        <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">
          Net balances
        </h2>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          {allSettled
            ? 'Everyone is settled up. Nice.'
            : 'Greens are owed money; reds owe money. Settle up suggestions live in the next tab.'}
        </p>
      </header>
      <ul className="space-y-3">
        {data.map((b) => (
          <BalanceRow key={b.userId} balance={b} scale={scale} />
        ))}
      </ul>
    </div>
  );
}
