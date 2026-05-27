# Splitwise-Lite — résumé bullets

Three packagings of the same project, in increasing word count. Pick
the one that fits the slot you're filling.

---

## Tight one-line

> **Splitwise-Lite** — full-stack group-expense splitter (Spring Boot 3.3
> + React 18 / TypeScript strict) with a two-heap debt-simplification
> algorithm that settles every group in ≤ N − 1 transfers; 59 unit
> tests, JWT-secured REST API, Dockerised Render + Vercel deploy.

---

## Three-bullet version (recommended for résumés)

- **Designed and shipped Splitwise-Lite end-to-end** — a Spring Boot 3.3
  + React 18 / TypeScript-strict mono-repo with three split modes
  (equal, exact, percentage) and a debt-simplification engine that
  produces ≤ N − 1 settle-up transfers in O(N log N) using two max-heaps,
  with deterministic remainder allocation guaranteeing byte-identical
  output across clients.
- **Built a hardened API surface** — stateless JWT auth (HS256 + BCrypt),
  RFC 7807 ProblemDetail errors, MDC correlation IDs per request, Flyway
  migrations, Spring Security with stateless filter chain, CORS
  restricted to a single configured origin, and OpenAPI-driven Swagger
  UI; structured the codebase as package-by-feature with strict
  `api → application → domain` layering.
- **Wrote 59 automated tests** — 38 JUnit 5 + Mockito tests covering the
  algorithmic core (SplitCalculator, DebtSimplifier, BalanceService) and
  21 Vitest + React Testing Library + MSW component tests covering
  login, currency utilities, the live split-summary helper, and the
  Balances tab; kept TypeScript strict mode (`noUncheckedIndexedAccess`
  on) and ESLint at zero warnings; shipped multi-stage Dockerfiles plus
  `render.yaml` + `vercel.json` for one-click deploy.

---

## Detailed four-bullet version (CV / cover-letter format)

- **Architecture and algorithms.** Designed and shipped Splitwise-Lite,
  a full-stack group-expense splitter, end-to-end across 8
  module-by-module commits. Implemented three split modes (equal,
  exact, percentage) with a `SplitCalculator` that does all money math
  in integer cents (BigDecimal at the boundary, `Math.multiplyExact` /
  `Math.addExact` overflow guards, deterministic remainder allocation
  to the smallest user IDs). Built a `DebtSimplifier` using two
  PriorityQueue max-heaps that settles every group in **≤ N − 1
  transfers** in **O(N log N)**, with a written correctness argument.

- **Backend (Java 21 + Spring Boot 3.3).** REST API with stateless
  JWT auth (HS256, JJWT 0.12) and BCrypt password hashing; Spring
  Security configured stateless with a custom `JwtAuthenticationFilter`
  populating `@AuthenticationPrincipal`. Authorization rule returns
  `404` instead of `403` for non-members to prevent group-id enumeration.
  All errors flow through a `GlobalExceptionHandler` emitting RFC 7807
  ProblemDetail; every request gets a correlation id stamped into the
  log pattern via MDC. JPA + Flyway 10 with a portable V1 schema that
  runs unchanged on PostgreSQL 16 and H2 in PostgreSQL mode for tests.
  Paginated list endpoints, `@EntityGraph` to avoid N+1, and a JPQL
  constructor expression to fetch group summaries in one round-trip.

- **Frontend (React 18 + TypeScript strict).** Vite 5 + Tailwind 3 +
  Headless UI accessible primitives, React Router v6 with nested
  group-detail tabs, TanStack Query for server state with
  `keepPreviousData` and explicit invalidation fan-out on mutations,
  Zustand persisted auth store (token + display profile only, never the
  password), axios interceptor that drops credentials on 401. Forms in
  React Hook Form + Zod; the Add Expense dialog computes a live
  validation message ("Shares sum to ₹950 — ₹50 short") that mirrors
  the backend `SplitCalculator` byte-for-byte. Dark mode, full keyboard
  navigation, focus-visible rings, WCAG-AA contrast on text.

- **Testing, tooling, deployment.** 38 JUnit 5 + Mockito + AssertJ
  tests on the backend and 21 Vitest + React Testing Library + MSW
  tests on the frontend, all green; covered every validation branch in
  `SplitCalculator`, the sum-invariant and ≤ N − 1 properties of
  `DebtSimplifier`, balance aggregation with settlement subtraction,
  and the live split-summary helper. ESLint flat config at zero
  warnings, TypeScript with `noUncheckedIndexedAccess`. Multi-stage
  Dockerfiles (Maven → JRE alpine for the API, Vite → nginx alpine for
  the SPA), `docker-compose up --build` for a working local stack,
  plus a `render.yaml` blueprint and `vercel.json` for one-click cloud
  deploy.
