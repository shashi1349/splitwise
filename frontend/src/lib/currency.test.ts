import { describe, expect, it } from 'vitest';
import {
  centsToDecimalString,
  formatPercentHundredths,
  parseAmountToCents,
  parsePercentToHundredths,
} from './currency';

describe('parseAmountToCents', () => {
  it('parses whole and fractional amounts into integer cents', () => {
    expect(parseAmountToCents('100')).toBe(10000);
    expect(parseAmountToCents('100.50')).toBe(10050);
    expect(parseAmountToCents('0.05')).toBe(5);
  });

  it('rejects malformed input', () => {
    expect(parseAmountToCents('abc')).toBeNaN();
    expect(parseAmountToCents('')).toBeNaN();
    expect(parseAmountToCents('1.234')).toBeNaN();
    expect(parseAmountToCents(undefined)).toBeNaN();
  });

  it('strips thousands separators', () => {
    expect(parseAmountToCents('1,234.56')).toBe(123456);
  });
});

describe('parsePercentToHundredths', () => {
  it('converts to hundredths-of-percent', () => {
    expect(parsePercentToHundredths('50')).toBe(5000);
    expect(parsePercentToHundredths('33.33')).toBe(3333);
    expect(parsePercentToHundredths('100.00')).toBe(10000);
  });

  it('returns NaN for invalid input', () => {
    expect(parsePercentToHundredths('xx')).toBeNaN();
    expect(parsePercentToHundredths('')).toBeNaN();
  });
});

describe('formatPercentHundredths and centsToDecimalString', () => {
  it('round-trips integer hundredths to "33.33"-style strings', () => {
    expect(formatPercentHundredths(3333)).toBe('33.33');
    expect(formatPercentHundredths(10000)).toBe('100.00');
    expect(formatPercentHundredths(5)).toBe('0.05');
  });

  it('converts integer cents to a 2-decimal string suitable for transport', () => {
    expect(centsToDecimalString(15050)).toBe('150.50');
    expect(centsToDecimalString(0)).toBe('0.00');
    expect(centsToDecimalString(7)).toBe('0.07');
    expect(centsToDecimalString(100000)).toBe('1000.00');
  });
});
