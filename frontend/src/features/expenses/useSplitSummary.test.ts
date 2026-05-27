import { describe, expect, it } from 'vitest';
import { computeSplitSummary } from './useSplitSummary';
import type { ShareRow } from './SplitInputs';

const everyone: ShareRow[] = [
  { userId: 1, included: true, amount: '', percent: '' },
  { userId: 2, included: true, amount: '', percent: '' },
  { userId: 3, included: true, amount: '', percent: '' },
];

describe('computeSplitSummary', () => {
  it('asks for participants when none are included', () => {
    const result = computeSplitSummary(
      'EQUAL',
      10000,
      'INR',
      everyone.map((r) => ({ ...r, included: false })),
    );
    expect(result.ok).toBe(false);
    expect(result.message).toMatch(/at least one participant/i);
  });

  it('reports a positive equal-split summary when total and participants are valid', () => {
    const result = computeSplitSummary('EQUAL', 30000, 'INR', everyone);
    expect(result.ok).toBe(true);
    expect(result.message).toMatch(/equally across 3/);
  });

  it('warns when EXACT shares fall short of the total', () => {
    const shares: ShareRow[] = [
      { userId: 1, included: true, amount: '50.00', percent: '' },
      { userId: 2, included: true, amount: '40.00', percent: '' },
      { userId: 3, included: true, amount: '5.00', percent: '' },
    ];
    const result = computeSplitSummary('EXACT', 10000, 'INR', shares);
    expect(result.ok).toBe(false);
    expect(result.message).toMatch(/short/i);
  });

  it('confirms when EXACT shares match the total', () => {
    const shares: ShareRow[] = [
      { userId: 1, included: true, amount: '40.00', percent: '' },
      { userId: 2, included: true, amount: '60.00', percent: '' },
    ];
    const result = computeSplitSummary('EXACT', 10000, 'INR', shares);
    expect(result.ok).toBe(true);
    expect(result.message).toMatch(/perfect/i);
  });

  it('reports the running percent total when PERCENT does not equal 100', () => {
    const shares: ShareRow[] = [
      { userId: 1, included: true, amount: '', percent: '50.00' },
      { userId: 2, included: true, amount: '', percent: '40.00' },
    ];
    const result = computeSplitSummary('PERCENT', 10000, 'INR', shares);
    expect(result.ok).toBe(false);
    expect(result.message).toMatch(/90\.00%.*short/i);
  });

  it('confirms when PERCENT shares add to 100%', () => {
    const shares: ShareRow[] = [
      { userId: 1, included: true, amount: '', percent: '33.33' },
      { userId: 2, included: true, amount: '', percent: '33.33' },
      { userId: 3, included: true, amount: '', percent: '33.34' },
    ];
    const result = computeSplitSummary('PERCENT', 10000, 'INR', shares);
    expect(result.ok).toBe(true);
    expect(result.message).toMatch(/100\.00%/);
  });

  it('rejects a zero or non-positive total', () => {
    const result = computeSplitSummary('EQUAL', 0, 'INR', everyone);
    expect(result.ok).toBe(false);
    expect(result.message).toMatch(/positive total/i);
  });
});
