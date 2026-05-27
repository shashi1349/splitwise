import { useGroupContext } from './useGroupContext';
import InviteMemberForm from './InviteMemberForm';

export default function MembersTab() {
  const { group, refetch } = useGroupContext();

  return (
    <div className="grid gap-6 lg:grid-cols-[2fr,3fr]">
      <section className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
        <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">
          Add a member
        </h2>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          We&apos;ll add them to this group right away.
        </p>
        <div className="mt-4">
          <InviteMemberForm groupId={group.id} onInvited={() => refetch()} />
        </div>
      </section>

      <section>
        <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">
          {group.members.length} {group.members.length === 1 ? 'member' : 'members'}
        </h2>
        <ul className="mt-3 divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white dark:divide-slate-800 dark:border-slate-800 dark:bg-slate-900">
          {group.members.map((m) => (
            <li
              key={m.userId}
              className="flex items-center justify-between gap-3 px-4 py-3"
            >
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-slate-900 dark:text-slate-100">
                  {m.displayName}
                </p>
                <p className="truncate text-xs text-slate-500 dark:text-slate-400">
                  {m.email}
                </p>
              </div>
              <span
                className={
                  m.role === 'OWNER'
                    ? 'rounded-full bg-brand-100 px-2 py-0.5 text-xs font-medium text-brand-700 dark:bg-brand-900/40 dark:text-brand-300'
                    : 'rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300'
                }
              >
                {m.role}
              </span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
