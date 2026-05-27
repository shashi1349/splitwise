import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import ThemeToggle from './ThemeToggle';
import Button from './ui/Button';

export default function AppShell() {
  const user = useAuthStore((s) => s.user);
  const clear = useAuthStore((s) => s.clear);
  const navigate = useNavigate();

  return (
    <div className="flex min-h-full flex-col">
      <header className="border-b border-slate-200 bg-white/80 backdrop-blur dark:border-slate-800 dark:bg-slate-950/80">
        <div className="mx-auto flex max-w-5xl items-center justify-between gap-3 px-4 py-3">
          <Link to="/" className="text-lg font-bold tracking-tight">
            Splitwise<span className="text-brand-600">·Lite</span>
          </Link>
          <nav className="flex items-center gap-3 text-sm">
            <ThemeToggle />
            {user && (
              <span className="hidden text-slate-500 dark:text-slate-400 sm:inline">
                {user.displayName}
              </span>
            )}
            {user && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  clear();
                  navigate('/login', { replace: true });
                }}
              >
                Sign out
              </Button>
            )}
          </nav>
        </div>
      </header>
      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  );
}
