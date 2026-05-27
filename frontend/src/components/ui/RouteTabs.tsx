import { NavLink } from 'react-router-dom';
import { cn } from '@/lib/cn';

export interface RouteTab {
  to: string;
  label: string;
  end?: boolean;
}

interface RouteTabsProps {
  tabs: RouteTab[];
}

export default function RouteTabs({ tabs }: RouteTabsProps) {
  return (
    <div className="border-b border-slate-200 dark:border-slate-800">
      <nav
        className="-mb-px flex gap-4 overflow-x-auto"
        aria-label="Tabs"
        role="tablist"
      >
        {tabs.map(({ to, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end ?? false}
            role="tab"
            className={({ isActive }) =>
              cn(
                'whitespace-nowrap border-b-2 px-1 pb-3 pt-2 text-sm font-medium transition-colors',
                isActive
                  ? 'border-brand-500 text-brand-700 dark:text-brand-400'
                  : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200',
              )
            }
          >
            {label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
