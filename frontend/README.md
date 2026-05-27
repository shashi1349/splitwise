# Splitwise-Lite — Frontend

React 18 + TypeScript strict + Vite + Tailwind. Feature-folder layout,
no business logic in components, all server state in TanStack Query, all
client state in Zustand, all forms in React Hook Form + Zod.

## Run

```bash
npm install
npm run dev      # http://localhost:5173
```

## Scripts

| Command            | Purpose                                       |
| ------------------ | --------------------------------------------- |
| `npm run dev`      | Vite dev server with HMR                      |
| `npm run build`    | Type-check (`tsc -b`) + production bundle     |
| `npm run lint`     | ESLint flat config (TS + React rules)         |
| `npm run typecheck`| Type-check only                               |
| `npm run format`   | Prettier in write mode                        |
| `npm test`         | Vitest single run (21 unit tests, all green)  |

## Container

```bash
docker build -t splitwise-frontend \
  --build-arg VITE_API_BASE_URL=http://localhost:8080 .
docker run --rm -p 5173:80 splitwise-frontend
```

The image builds the production bundle in a `node:22-alpine` stage and
serves it from `nginx:1.27-alpine` with the SPA fallback configured in
`nginx.conf`.

## Configuration

| Env var               | Where it's read              | Effect |
| --------------------- | ---------------------------- | ------ |
| `VITE_API_BASE_URL`   | `src/api/client.ts`          | Backend base URL. Must be set at build time on Vercel. |

## Where the interesting code lives

- `src/api/client.ts` — axios instance with JWT request interceptor and
  401 handler that clears the auth store.
- `src/store/authStore.ts` — Zustand persist (token + display profile,
  never the password).
- `src/lib/currency.ts` — `parseAmountToCents`, `centsToDecimalString`,
  cached `Intl.NumberFormat` formatters. **Never `parseFloat`.**
- `src/features/expenses/AddExpenseDialog.tsx` — the most-used screen,
  with live `computeSplitSummary` mirroring the backend's SplitCalculator.
- `src/features/balances/BalancesTab.tsx` — centred-axis green/red bar
  visualisation with English status lines.
- `src/features/settlements/SettleUpTab.tsx` — `Mark as paid` button
  that posts a settlement and invalidates balances + settle-up + history
  queries in one call.

## Tests

Vitest + React Testing Library + MSW. Setup in `src/test/`:
`server.ts` (MSW server), `handlers.ts` (default handlers),
`utils.tsx` (`renderWithProviders`), `setup.ts` (lifecycle hooks).
21 tests covering currency utils, the live split-summary helper,
LoginPage form behaviour, and BalancesTab rendering states.
