import { Link } from 'react-router-dom';
import type { GroupSummary } from '@/api/groups';
import { cn } from '@/lib/cn';

interface GroupCardProps {
  group: GroupSummary;
}

export default function GroupCard({ group }: GroupCardProps) {
  return (
    <Link
      to={`/groups/${group.id}`}
      className={cn(
        'group block rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition',
        'hover:border-brand-300 hover:shadow-md',
        'dark:border-slate-800 dark:bg-slate-900 dark:hover:border-brand-700',
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <h3 className="line-clamp-1 text-base font-semibold text-slate-900 group-hover:text-brand-700 dark:text-slate-100 dark:group-hover:text-brand-400">
          {group.name}
        </h3>
        <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
          {group.currencyCode}
        </span>
      </div>
      <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
        {group.memberCount} {group.memberCount === 1 ? 'member' : 'members'} ·{' '}
        {group.myRole === 'OWNER' ? 'You created this' : 'You are a member'}
      </p>
    </Link>
  );
}
