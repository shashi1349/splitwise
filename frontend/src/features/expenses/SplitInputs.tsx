import type { MemberDto } from '@/api/groups';
import type { SplitType } from '@/api/expenses';
import { cn } from '@/lib/cn';

export interface ShareRow {
  userId: number;
  included: boolean;
  amount: string;
  percent: string;
}

interface SplitInputsProps {
  members: MemberDto[];
  splitType: SplitType;
  shares: ShareRow[];
  onChange: (shares: ShareRow[]) => void;
}

export default function SplitInputs({
  members,
  splitType,
  shares,
  onChange,
}: SplitInputsProps) {
  const updateRow = (userId: number, patch: Partial<ShareRow>) => {
    onChange(shares.map((s) => (s.userId === userId ? { ...s, ...patch } : s)));
  };

  return (
    <div className="rounded-md border border-slate-200 dark:border-slate-800">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500 dark:bg-slate-900 dark:text-slate-400">
          <tr>
            <th className="w-10 px-3 py-2"></th>
            <th className="px-3 py-2">Participant</th>
            {splitType === 'EXACT' && <th className="w-32 px-3 py-2 text-right">Amount</th>}
            {splitType === 'PERCENT' && <th className="w-32 px-3 py-2 text-right">Percent</th>}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
          {members.map((m) => {
            const row = shares.find((s) => s.userId === m.userId);
            if (!row) return null;
            const inputId = `share-${m.userId}`;
            return (
              <tr
                key={m.userId}
                className={cn(!row.included && 'opacity-50')}
              >
                <td className="px-3 py-2">
                  <input
                    type="checkbox"
                    id={`include-${m.userId}`}
                    aria-label={`Include ${m.displayName}`}
                    checked={row.included}
                    onChange={(e) =>
                      updateRow(m.userId, { included: e.target.checked })
                    }
                    className="h-4 w-4 rounded border-slate-300 text-brand-600 focus:ring-brand-500"
                  />
                </td>
                <td className="px-3 py-2">
                  <label
                    htmlFor={`include-${m.userId}`}
                    className="font-medium text-slate-900 dark:text-slate-100"
                  >
                    {m.displayName}
                  </label>
                </td>
                {splitType === 'EXACT' && (
                  <td className="px-3 py-2 text-right">
                    <input
                      id={inputId}
                      type="text"
                      inputMode="decimal"
                      autoComplete="off"
                      placeholder="0.00"
                      disabled={!row.included}
                      aria-label={`Amount for ${m.displayName}`}
                      value={row.amount}
                      onChange={(e) =>
                        updateRow(m.userId, { amount: e.target.value })
                      }
                      className="w-24 rounded-md border border-slate-300 bg-white px-2 py-1 text-right text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/30 disabled:cursor-not-allowed disabled:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:disabled:bg-slate-950"
                    />
                  </td>
                )}
                {splitType === 'PERCENT' && (
                  <td className="px-3 py-2 text-right">
                    <input
                      id={inputId}
                      type="text"
                      inputMode="decimal"
                      autoComplete="off"
                      placeholder="0.00%"
                      disabled={!row.included}
                      aria-label={`Percent for ${m.displayName}`}
                      value={row.percent}
                      onChange={(e) =>
                        updateRow(m.userId, { percent: e.target.value })
                      }
                      className="w-24 rounded-md border border-slate-300 bg-white px-2 py-1 text-right text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/30 disabled:cursor-not-allowed disabled:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:disabled:bg-slate-950"
                    />
                  </td>
                )}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
