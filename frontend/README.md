# Splitwise-Lite — Frontend

React 18 + TypeScript (strict) + Vite + Tailwind CSS.

## Run

```bash
npm install
npm run dev      # http://localhost:5173
```

## Scripts

| Command            | Purpose                                 |
| ------------------ | --------------------------------------- |
| `npm run dev`      | Vite dev server with HMR                |
| `npm run build`    | Type-check (`tsc -b`) + production bundle |
| `npm run lint`     | ESLint (flat config, TS + React rules)  |
| `npm run typecheck`| Type-check only                         |
| `npm run format`   | Prettier in write mode                  |
| `npm test`         | Vitest single run (added in Module 7)   |

## Environment

Copy `.env.example` to `.env.local` and adjust `VITE_API_BASE_URL`.
