export default function HomePage() {
  return (
    <main className="mx-auto flex h-full max-w-3xl flex-col items-center justify-center gap-4 px-6 text-center">
      <span className="rounded-full bg-brand-50 px-3 py-1 text-xs font-medium uppercase tracking-wide text-brand-700 dark:bg-brand-900/30 dark:text-brand-300">
        Module 1 — Bootstrap
      </span>
      <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">
        Splitwise-Lite
      </h1>
      <p className="max-w-xl text-base text-slate-600 dark:text-slate-300">
        Split group expenses, see who owes what, and settle up in the fewest
        possible transfers.
      </p>
      <p className="text-sm text-slate-500">
        Auth, groups, expenses, balances, and debt simplification arrive in the
        next modules.
      </p>
    </main>
  );
}
