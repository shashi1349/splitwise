import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { listGroups } from '@/api/groups';
import { getProblemDetail } from '@/api/client';
import Button from '@/components/ui/Button';
import EmptyState from '@/components/EmptyState';
import Skeleton from '@/components/Skeleton';
import GroupCard from './GroupCard';
import CreateGroupDialog from './CreateGroupDialog';

export default function GroupsPage() {
  const [createOpen, setCreateOpen] = useState(false);
  const { data, isLoading, error } = useQuery({
    queryKey: ['groups'],
    queryFn: listGroups,
  });

  return (
    <section className="mx-auto max-w-5xl px-4 py-8 sm:px-6">
      <header className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Your groups</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Track expenses with friends and roommates.
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>New group</Button>
      </header>

      {isLoading && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} className="h-24 w-full" />
          ))}
        </div>
      )}

      {error && (
        <p
          role="alert"
          className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
        >
          Could not load groups: {getProblemDetail(error).detail ?? 'unknown error'}
        </p>
      )}

      {data && data.length === 0 && (
        <EmptyState
          title="No groups yet"
          description="Create your first group to start tracking shared expenses."
          action={
            <Button onClick={() => setCreateOpen(true)}>Create a group</Button>
          }
        />
      )}

      {data && data.length > 0 && (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((g) => (
            <li key={g.id}>
              <GroupCard group={g} />
            </li>
          ))}
        </ul>
      )}

      <CreateGroupDialog open={createOpen} onClose={() => setCreateOpen(false)} />
    </section>
  );
}
