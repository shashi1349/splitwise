import { useAuthStore } from '@/store/authStore';

export default function HomePage() {
  const user = useAuthStore((s) => s.user);

  return (
    <section className="mx-auto max-w-3xl px-6 py-10">
      <header className="space-y-2">
        <span className="rounded-full bg-brand-50 px-3 py-1 text-xs font-medium uppercase tracking-wide text-brand-700 dark:bg-brand-900/30 dark:text-brand-300">
          Module 2 — Auth
        </span>
        <h1 className="text-3xl font-bold tracking-tight">
          {user ? `Welcome back, ${user.displayName}.` : 'Welcome.'}
        </h1>
        <p className="max-w-xl text-base text-slate-600 dark:text-slate-300">
          You are signed in as{' '}
          <span className="font-medium">{user?.email}</span>. Groups, expenses,
          balances, and the debt-simplification settle-up flow arrive in the
          next modules.
        </p>
      </header>
    </section>
  );
}
