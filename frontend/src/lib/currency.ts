/**
 * Currency helpers — never use {@code parseFloat} on user input.
 * Amounts move between integer cents (storage) and BigDecimal-like
 * strings (transport / display) without ever entering binary floating point.
 */

const FORMAT_CACHE = new Map<string, Intl.NumberFormat>();

function formatter(currency: string): Intl.NumberFormat {
  const key = `${navigator.language}::${currency}`;
  let f = FORMAT_CACHE.get(key);
  if (!f) {
    f = new Intl.NumberFormat(navigator.language, {
      style: 'currency',
      currency,
      maximumFractionDigits: 2,
      minimumFractionDigits: 2,
    });
    FORMAT_CACHE.set(key, f);
  }
  return f;
}

/** Format an integer-cents amount for display. */
export function formatMoney(cents: number, currency: string): string {
  return formatter(currency).format(cents / 100);
}

/**
 * Parse a user-entered amount string ("1,234.56") into integer cents.
 * Returns NaN for malformed input. Accepts at most 2 decimal places.
 */
export function parseAmountToCents(input: string | undefined | null): number {
  if (input == null) return NaN;
  const cleaned = input.replace(/,/g, '').trim();
  if (cleaned === '') return NaN;
  if (!/^\d+(\.\d{1,2})?$/.test(cleaned)) return NaN;
  const [whole = '0', fracRaw = ''] = cleaned.split('.');
  const padded = (fracRaw + '00').slice(0, 2);
  const wholeNum = Number(whole);
  const fracNum = Number(padded);
  if (!Number.isFinite(wholeNum) || !Number.isFinite(fracNum)) return NaN;
  return wholeNum * 100 + fracNum;
}

/** Parse a percent string ("33.33") into integer hundredths-of-percent. */
export function parsePercentToHundredths(input: string | undefined | null): number {
  if (input == null) return NaN;
  const cleaned = input.replace(/,/g, '').trim();
  if (cleaned === '') return NaN;
  if (!/^\d+(\.\d{1,2})?$/.test(cleaned)) return NaN;
  const [whole = '0', fracRaw = ''] = cleaned.split('.');
  const padded = (fracRaw + '00').slice(0, 2);
  return Number(whole) * 100 + Number(padded);
}

/** Render a percent stored as integer hundredths back to a "33.33" string. */
export function formatPercentHundredths(hundredths: number): string {
  const sign = hundredths < 0 ? '-' : '';
  const abs = Math.abs(hundredths);
  const whole = Math.trunc(abs / 100);
  const frac = (abs % 100).toString().padStart(2, '0');
  return `${sign}${whole}.${frac}`;
}
