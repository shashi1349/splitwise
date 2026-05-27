import { Outlet, useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getGroup } from '@/api/groups';
import { getProblemDetail } from '@/api/client';
import RouteTabs from '@/components/ui/RouteTabs';
import Skeleton from '@/components/Skeleton';
import type { GroupContext } from './useGroupContext';

export default function GroupDetailPage() {
  const params = useParams();
  const groupId = params.groupId ?? '';

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['group', groupId],
    queryFn: () => getGroup(Number(groupId)),
    enabled: groupId.length > 0,
  });

  if (isLoading) {
    return (
      <section className="mx-auto max-w-5xl px-4 py-8 sm:px-6">
        <Skeleton className="h-8 w-1/3" />
        <Skeleton className="mt-4 h-10 w-full" />
        <Skeleton className="mt-6 h-32 w-full" />
      </section>
    );
  }

  if (error || !data) {
    const problem = getProblemDetail(error);
    return (
      <section className="mx-auto max-w-3xl px-4 py-12 text-center sm:px-6">
        <h2 className="text-xl font-semibold text-slate-900 dark:text-slate-100">
          {problem.status === 404 ? 'Group not found' : 'Could not load group'}
        </h2>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          {problem.detail ?? 'Try again in a moment.'}
        </p>
        <Link
          to="/groups"
          className="mt-4 inline-block text-sm font-medium text-brand-600 hover:underline"
        >
          Back to groups
        </Link>
      </section>
    );
  }

  const tabs = [
    { to: 'expenses', label: 'Expenses' },
    { to: 'balances', label: 'Balances' },
    { to: 'settle', label: 'Settle up' },
    { to: 'members', label: `Members (${data.members.length})` },
  ];

  const context: GroupContext = { group: data, refetch };

  return (
    <section className="mx-auto max-w-5xl px-4 py-8 sm:px-6">
      <header className="mb-4">
        <Link
          to="/groups"
          className="text-sm text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200"
        >
          &larr; All groups
        </Link>
        <div className="mt-1 flex flex-wrap items-baseline gap-3">
          <h1 className="text-2xl font-bold tracking-tight">{data.name}</h1>
          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
            {data.currencyCode}
          </span>
        </div>
      </header>
      <RouteTabs tabs={tabs} />
      <div className="py-6">
        <Outlet context={context} />
      </div>
    </section>
  );
}
