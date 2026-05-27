import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef9f4',
          100: '#d6f0e3',
          200: '#aee0c7',
          300: '#7ccba8',
          400: '#46b389',
          500: '#10b981',
          600: '#0d966b',
          700: '#0a7251',
          800: '#08573e',
          900: '#053a29',
        },
      },
      fontFamily: {
        sans: ['ui-sans-serif', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
} satisfies Config;
