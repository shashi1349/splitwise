import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createExpense, type SplitType } from '@/api/expenses';
import { getProblemDetail } from '@/api/client';
import type { GroupDetail } from '@/api/groups';
import { useAuthStore } from '@/store/authStore';
import { parseAmountToCents } from '@/lib/currency';
import Dialog from '@/components/ui/Dialog';
import FormField from '@/components/ui/FormField';
import Select from '@/components/ui/Select';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import SplitInputs, { type ShareRow } from './SplitInputs';
import { computeSplitSummary } from './useSplitSummary';
import { cn } from '@/lib/cn';

interface AddExpenseDialogProps {
  open: boolean;
  onClose: () => void;
  group: GroupDetail;
}

const SPLIT_TYPES: { value: SplitType; label: string; hint: string }[] = [
  { value: 'EQUAL', label: 'Equal', hint: 'Split evenly across participants.' },
  { value: 'EXACT', label: 'Exact', hint: 'Enter the amount each person owes.' },
  { value: 'PERCENT', label: 'Percent', hint: 'Enter percentages summing to 100.' },
];

function makeInitialShares(group: GroupDetail): ShareRow[] {
  return group.members.map((m) => ({
    userId: m.userId,
    included: true,
    amount: '',
    percent: '',
  }));
}

export default function AddExpenseDialog({ open, onClose, group }: AddExpenseDialogProps) {
  const queryClient = useQueryClient();
  const me = useAuthStore((s) => s.user);

  const defaultPayer =
    group.members.find((m) => m.userId === me?.id)?.userId ?? group.members[0]?.userId ?? 0;

  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [payerId, setPayerId] = useState<number>(defaultPayer);
  const [splitType, setSplitType] = useState<SplitType>('EQUAL');
  const [shares, setShares] = useState<ShareRow[]>(() => makeInitialShares(group));
  const [serverError, setServerError] = useState<string | null>(null);

  // Reset whenever the dialog re-opens or the group's member list changes.
  useEffect(() => {
    if (open) {
      setDescription('');
      setAmount('');
      setPayerId(defaultPayer);
      setSplitType('EQUAL');
      setShares(makeInitialShares(group));
      setServerError(null);
    }
  }, [open, group, defaultPayer]);

  const totalCents = parseAmountToCents(amount);
  const summary = useMemo(
    () => computeSplitSummary(splitType, totalCents, group.currencyCode, shares),
    [splitType, totalCents, group.currencyCode, shares],
  );

  const mutation = useMutation({
    mutationFn: () =>
      createExpense(group.id, {
        description: description.trim(),
        amount,
        payerId,
        splitType,
        shares: shares
          .filter((s) => s.included)
          .map((s) => {
            if (splitType === 'EXACT') return { userId: s.userId, amount: s.amount };
            if (splitType === 'PERCENT') return { userId: s.userId, percent: s.percent };
            return { userId: s.userId };
          }),
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['expenses', group.id] });
      await queryClient.invalidateQueries({ queryKey: ['balances', group.id] });
      await queryClient.invalidateQueries({ queryKey: ['settle', group.id] });
      onClose();
    },
    onError: (err) =>
      setServerError(getProblemDetail(err).detail ?? 'Could not save the expense.'),
  });

  const descriptionValid = description.trim().length > 0;
  const amountValid = Number.isFinite(totalCents) && totalCents > 0;
  const canSubmit = descriptionValid && amountValid && summary.ok && !mutation.isPending;

  return (
    <Dialog
      open={open}
      onClose={() => !mutation.isPending && onClose()}
      title="Add an expense"
      description={`Group "${group.name}" · ${group.currencyCode}`}
      className="max-w-xl"
    >
      <form
        onSubmit={(e) => {
          e.preventDefault();
          setServerError(null);
          if (canSubmit) mutation.mutate();
        }}
        className="flex flex-col gap-4"
        noValidate
      >
        <FormField
          label="Description"
          id="exp-description"
          autoComplete="off"
          maxLength={255}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          error={!descriptionValid && description.length > 0 ? 'Description is required' : undefined}
        />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <FormField
            label={`Amount (${group.currencyCode})`}
            id="exp-amount"
            inputMode="decimal"
            autoComplete="off"
            placeholder="0.00"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            error={amount.length > 0 && !amountValid ? 'Use up to 2 decimal places' : undefined}
          />
          <Select
            label="Paid by"
            id="exp-payer"
            value={payerId}
            onChange={(e) => setPayerId(Number(e.target.value))}
          >
            {group.members.map((m) => (
              <option key={m.userId} value={m.userId}>
                {m.displayName}
              </option>
            ))}
          </Select>
        </div>

        <fieldset className="grid grid-cols-3 gap-2">
          <legend className="mb-1 text-sm font-medium text-slate-700 dark:text-slate-200">
            Split type
          </legend>
          {SPLIT_TYPES.map(({ value, label, hint }) => (
            <label
              key={value}
              title={hint}
              className={cn(
                'cursor-pointer rounded-md border px-3 py-2 text-center text-sm font-medium transition-colors',
                splitType === value
                  ? 'border-brand-500 bg-brand-50 text-brand-700 dark:border-brand-400 dark:bg-brand-900/30 dark:text-brand-300'
                  : 'border-slate-300 text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800',
              )}
            >
              <input
                type="radio"
                name="splitType"
                value={value}
                checked={splitType === value}
                onChange={() => setSplitType(value)}
                className="sr-only"
              />
              {label}
            </label>
          ))}
        </fieldset>

        <SplitInputs
          members={group.members}
          splitType={splitType}
          shares={shares}
          onChange={setShares}
        />

        <p
          role="status"
          aria-live="polite"
          className={cn(
            'rounded-md px-3 py-2 text-sm',
            summary.ok
              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300'
              : 'bg-amber-50 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300',
          )}
        >
          {summary.message}
        </p>

        {serverError && (
          <p
            role="alert"
            className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
          >
            {serverError}
          </p>
        )}

        <div className="flex items-center justify-end gap-2">
          <Button
            type="button"
            variant="secondary"
            onClick={onClose}
            disabled={mutation.isPending}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={!canSubmit}>
            {mutation.isPending ? (
              <>
                <Spinner className="mr-2" />
                Saving
              </>
            ) : (
              'Save expense'
            )}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
