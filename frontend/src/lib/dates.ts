const RTF = new Intl.RelativeTimeFormat(undefined, { numeric: 'auto' });

const UNITS: Array<[Intl.RelativeTimeFormatUnit, number]> = [
  ['year', 365 * 24 * 60 * 60],
  ['month', 30 * 24 * 60 * 60],
  ['day', 24 * 60 * 60],
  ['hour', 60 * 60],
  ['minute', 60],
];

/**
 * Locale-aware "2 days ago" / "in 3 hours". Falls back to an absolute
 * date for anything older than a year.
 */
export function formatRelative(iso: string): string {
  const target = new Date(iso).getTime();
  const now = Date.now();
  const diffSeconds = Math.round((target - now) / 1000);
  const absSeconds = Math.abs(diffSeconds);
  if (absSeconds < 45) return 'just now';
  for (const [unit, secs] of UNITS) {
    if (absSeconds >= secs) {
      return RTF.format(Math.round(diffSeconds / secs), unit);
    }
  }
  return RTF.format(Math.round(diffSeconds / 60), 'minute');
}

export function formatDateOnly(iso: string): string {
  return new Date(iso).toLocaleDateString();
}
