import type { SplitType } from '@/api/expenses';
import {
  formatMoney,
  formatPercentHundredths,
  parseAmountToCents,
  parsePercentToHundredths,
} from '@/lib/currency';
import type { ShareRow } from './SplitInputs';

export interface SplitSummary {
  ok: boolean;
  message: string;
}

/**
 * Live validation message rendered under the Add Expense form.
 * Mirrors the server-side rules in {@code SplitCalculator}.
 */
export function computeSplitSummary(
  splitType: SplitType,
  totalCents: number,
  currency: string,
  shares: ShareRow[],
): SplitSummary {
  const included = shares.filter((s) => s.included);
  if (included.length === 0) {
    return { ok: false, message: 'Pick at least one participant.' };
  }
  if (!Number.isFinite(totalCents) || totalCents <= 0) {
    return { ok: false, message: 'Enter a positive total first.' };
  }

  if (splitType === 'EQUAL') {
    return {
      ok: true,
      message: `Splitting ${formatMoney(totalCents, currency)} equally across ${included.length} ${
        included.length === 1 ? 'person' : 'people'
      }.`,
    };
  }

  if (splitType === 'EXACT') {
    let sum = 0;
    let anyInvalid = false;
    for (const s of included) {
      const c = parseAmountToCents(s.amount);
      if (!Number.isFinite(c) || c < 0) {
        anyInvalid = true;
        break;
      }
      sum += c;
    }
    if (anyInvalid) {
      return { ok: false, message: 'Enter a valid amount for each included participant.' };
    }
    const diff = totalCents - sum;
    if (diff === 0) {
      return { ok: true, message: `Shares sum to ${formatMoney(sum, currency)} — perfect.` };
    }
    if (diff > 0) {
      return {
        ok: false,
        message: `Shares sum to ${formatMoney(sum, currency)} — ${formatMoney(diff, currency)} short.`,
      };
    }
    return {
      ok: false,
      message: `Shares sum to ${formatMoney(sum, currency)} — ${formatMoney(-diff, currency)} over.`,
    };
  }

  // PERCENT
  let sumHundredths = 0;
  let anyInvalid = false;
  for (const s of included) {
    const h = parsePercentToHundredths(s.percent);
    if (!Number.isFinite(h) || h < 0) {
      anyInvalid = true;
      break;
    }
    sumHundredths += h;
  }
  if (anyInvalid) {
    return { ok: false, message: 'Enter a valid percent for each included participant.' };
  }
  if (sumHundredths === 10000) {
    return { ok: true, message: 'Percentages add up to 100.00%.' };
  }
  const diff = 10000 - sumHundredths;
  return {
    ok: false,
    message:
      diff > 0
        ? `Percentages sum to ${formatPercentHundredths(sumHundredths)}% — ${formatPercentHundredths(diff)}% short of 100%.`
        : `Percentages sum to ${formatPercentHundredths(sumHundredths)}% — ${formatPercentHundredths(-diff)}% over 100%.`,
  };
}
