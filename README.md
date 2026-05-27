# Splitwise-Lite

Group-expense splitter with a **debt-simplification** algorithm that
settles every group in **at most N − 1 transfers**, where N is the
number of people with non-zero balances. Built as a production-grade
mono-repo: Java 21 + Spring Boot 3.3 backend, React 18 + TypeScript
strict frontend, ~3.5k LoC excluding tests, **59 unit tests**, RFC 7807
errors, JWT auth, Flyway migrations, Docker, and one-click Render +
Vercel deploys.

> **Live demo:** [add backend URL] · [add frontend URL]
> Real screenshots and live links land here once the repo owner runs the
> [DEPLOYMENT.md](./DEPLOYMENT.md) steps. The local stack is fully
> reproducible with one command (see *Quick start* below).

---

## Pitch

You're on a trip. Alice paid for the hotel, Bob paid for the cab,
Carol paid for dinner. Now what? **Splitwise-Lite** keeps track of
everyone's expenses, computes who owes whom, and tells you the
**fewest possible transfers** to make everyone settle up.

Three split modes: **equal** (default), **exact amounts**, **percentage**.
Money math is integer cents end-to-end so you never see `9.999999999`
on screen. Every API error is RFC 7807. Every request gets a
correlation id. Every interesting bit has a unit test.

---

## Architecture

```
┌──────────────────────┐     HTTPS / Bearer JWT      ┌───────────────────────┐
│   React 18 SPA       │ ──────────────────────────► │  Spring Boot 3.3 API  │
│   Vite · Tailwind    │ ◄────────────────────────── │  ProblemDetail JSON   │
│   React Query cache  │                             │  Stateless JWT chain  │
│   Zustand auth store │                             │  Method security      │
│   RHF + Zod forms    │                             │  Correlation MDC      │
└──────────────────────┘                             └─────────────┬─────────┘
                                                                   │ JPA · Flyway
                                                                   ▼
                                                    ┌────────────────────────┐
                                                    │  PostgreSQL 16         │
                                                    │  (H2 PG-mode in dev)   │
                                                    └────────────────────────┘
```

Backend follows **package-by-feature** (`auth/`, `user/`, `group/`,
`expense/`, `balance/`, `settlement/`, `common/`) with a strict
`api → application → domain` layering inside each feature. Frontend
follows **feature-folder** (`features/groups/`, `features/expenses/`,
`features/balances/`, `features/settlements/`) plus shared
`components/`, `hooks/`, `lib/`.

---

## Tech stack

| Layer        | Choice                                                         | Why |
| ------------ | -------------------------------------------------------------- | --- |
| **Language** | Java 21 (records, pattern matching, sealed types) · TypeScript 5.5 strict | Strong types catch shape errors before the network. |
| **Backend**  | Spring Boot 3.3.5 — web, validation, data-jpa, security, actuator | Boring, batteries-included, well-known. |
| **Auth**     | Stateless JWT (HS256, JJWT 0.12) + BCrypt                      | No server session = trivially horizontally scalable. |
| **Persistence** | JPA + Hibernate 6.5 + Flyway 10                              | Migrations are checked-in code, not click-ops. |
| **DB**       | PostgreSQL 16 (prod) · H2 in PostgreSQL mode (dev/tests)       | Identical SQL surface in V1 schema means tests trust prod. |
| **Errors**   | Spring `ProblemDetail` (RFC 7807)                              | Unambiguous, machine-readable error contract. |
| **Docs**     | springdoc-openapi 2.6 (Swagger UI)                             | Single source of truth for the API. |
| **Frontend** | React 18 · Vite 5 · Tailwind 3.4 · Headless UI                 | Fast HMR, accessible primitives, tree-shaken bundle. |
| **State**    | TanStack Query (server) · Zustand (auth)                       | Server cache vs client state, separated. |
| **Forms**    | React Hook Form + Zod                                          | Schema-validated input, no `any`. |
| **Testing**  | JUnit 5 · Mockito · AssertJ · Vitest · RTL · MSW               | 38 backend + 21 frontend unit tests, all green. |
| **Build**    | Maven 3.9 · Vite · multi-stage Docker                          | One image per service, tiny runtime layers. |
| **Deploy**   | Render (Docker + Postgres) · Vercel (static)                   | Free tiers, blueprint files in repo. |

---

## Quick start

Requires Docker (or: Java 21 + Maven 3.9 + Node 20+ for native).

```bash
git clone https://github.com/shashi1349/splitwise.git
cd splitwise
docker compose up --build
```

| What        | Where                                       |
| ----------- | ------------------------------------------- |
| Frontend    | https://splitwise-omega-blue.vercel.app/    |
| Backend API | https://splitwise-m0fx.onrender.com         |


### Native (no Docker)

```bash
# Terminal 1 — backend (uses in-memory H2)
cd backend && mvn spring-boot:run

# Terminal 2 — frontend
cd frontend && npm install && npm run dev
```

### Test

```bash
cd backend  && mvn test     # 38 tests
cd frontend && npm test     # 21 tests
```

---

## Walkthrough

1. Register `alice@example.com` and `bob@example.com` (two browsers or
   private windows).
2. Alice creates the group **Goa Trip** (currency INR).
3. Alice invites Bob by email.
4. Alice clicks **Add expense**, enters `Hotel`, `300.00`, payer = Alice,
   split = Equal — both members are pre-selected.
5. Bob signs in. The new group appears on his Groups page.
6. Bob adds `Cab`, `60.00`, payer = Bob, split = Equal.
7. Open the **Balances** tab — Alice is owed ₹120.00, Bob owes ₹120.00.
   The bar visualizes magnitudes; the line under it spells out
   "Alice is owed ₹120.00".
8. Open the **Settle up** tab — one suggestion: **Bob → Alice ₹120.00**.
   Click **Mark as paid**. Balances update to zero, the suggestion
   disappears, and the settlement is logged in the history below.

---

## API reference

Full interactive docs live at `/swagger-ui.html`. The shape of every
endpoint is generated from the controllers, so the doc never lies.

| Method | Path                                | Auth | Purpose |
| ------ | ----------------------------------- | ---- | ------- |
| `POST` | `/auth/register`                    | —    | Create a user, return a JWT. |
| `POST` | `/auth/login`                       | —    | Verify password, return a JWT. |
| `POST` | `/groups`                           | ✓    | Create a group (creator auto-enrolled as OWNER). |
| `GET`  | `/groups`                           | ✓    | List groups the caller belongs to. |
| `GET`  | `/groups/{id}`                      | ✓    | Group detail (members included). |
| `GET`  | `/groups/{id}/members`              | ✓    | List members. |
| `POST` | `/groups/{id}/members`              | ✓    | Invite by email (must be a registered user). |
| `POST` | `/groups/{id}/expenses`             | ✓    | Create an expense (EQUAL / EXACT / PERCENT). |
| `GET`  | `/groups/{id}/expenses`             | ✓    | Paginated expense list. |
| `GET`  | `/groups/{id}/balances`             | ✓    | Per-member net balance, creditors first. |
| `GET`  | `/groups/{id}/settle-up`            | ✓    | Suggested minimum-transfer settlements. |
| `POST` | `/groups/{id}/settlements`          | ✓    | Record a paid settlement. |
| `GET`  | `/groups/{id}/settlements`          | ✓    | Settlement history (newest first). |

Authorization: every protected endpoint requires `Authorization: Bearer <jwt>`.
Responses on error are always `application/problem+json` per RFC 7807.

---

## Design decisions

A condensed log of choices and trade-offs.

### 1. Money is always integer cents on the wire and in code

`BigDecimal` survives JSON serialization fine, but binary floating-point
*never* enters the picture: requests are validated with
`@Digits(integer=12, fraction=2)`, internal math is `long` cents, and the
SplitCalculator uses `Math.multiplyExact` / `Math.addExact` to prevent
silent overflow. The frontend mirrors this with `parseAmountToCents` and
`centsToDecimalString`.

### 2. SplitCalculator guarantees `Σshares = total`

For EQUAL and PERCENT, integer-floor produces a deficit of up to
`N − 1` cents. The remainder is allocated **one cent at a time to the
participants with the smallest userIds** so two clients computing the
same split agree byte-for-byte. EXACT throws if the caller's input
doesn't sum to the total — we never silently round.

### 3. Debt simplification: greedy with two max-heaps, ≤ N−1 transfers

Build a max-heap of creditors (by net amount) and a max-heap of debtors
(by absolute net). Pop the largest of each, transfer `min(c, d)`, push
back any leftover. Whichever side fully settles is removed permanently,
so each iteration eliminates ≥ 1 party. After at most N − 1 iterations
only one party can remain, and since `Σ balances = 0` it must be at
zero. Time `O(N log N)`. Tie-breaking on `userId` makes the output
deterministic across processes.

### 4. Authorization rule: **404, not 403, for non-members**

If the caller isn't a member of a group, every group-scoped endpoint
returns `404 Group not found.` instead of `403 Forbidden.`. This costs
nothing in UX (legitimate users see only groups they belong to via
`GET /groups`) and prevents enumeration of group ids.

### 5. Errors are RFC 7807 ProblemDetail, end-to-end

Backend returns `application/problem+json` for *every* failure:
validation, auth, conflicts, generic 500. The frontend has a single
helper `getProblemDetail(err)` that yields a typed object, so toast/
alert components never see raw axios errors.

### 6. Stateless JWT, no refresh tokens

JWTs expire in 2 hours. Refresh tokens add complexity (revocation
lists, sliding sessions) without benefit at this scale. If a token is
stolen the impact window is 2 hours, which matches typical IDPs.

### 7. Pagination from day one on the expenses endpoint

Once a group accumulates a year of dinners the unbounded list dies. The
controller uses Spring `Pageable` and the response is wrapped in a
hand-rolled `PageResponse<T>` so the JSON shape doesn't leak Spring
internals (`pageable.sort.unsorted` and friends).

### 8. ESLint zero-warning, TypeScript strict + `noUncheckedIndexedAccess`

This setting alone catches more bugs than most teams realize: every
`array[i]` is `T | undefined`, every `map.get(k)` similarly, so the
compiler forces explicit nullability instead of letting it leak into
runtime as `Cannot read property of undefined`.

---

## What I'd build next

In rough priority order:

1. **Pending invitations.** Today inviting someone requires them to
   be already registered. A `pending_invites` table keyed on email +
   group plus a claim-on-signup flow would close that.
2. **Multi-currency per expense.** Group has a single currency; for
   travel you sometimes pay in USD on a Mumbai trip. Add an FX
   conversion at expense-time, stored alongside the expense.
3. **Server-Sent Events for live balances.** When Bob adds an expense,
   Alice's open Balances tab shouldn't need a manual refetch. SSE from
   the backend + React Query's `setQueryData` would do it.
4. **Receipt photos.** S3 (or Cloudflare R2) + a presigned-URL upload
   flow + a thumbnail in the expense card.
5. **Pluggable identity.** Add Google + GitHub OAuth via Spring's
   OAuth2 client. Keep BCrypt as the local fallback.
6. **Native iOS app sharing the same API.** Useful demo for "I designed
   the contract carefully so a mobile client can come for free."

---

## Repo map

```
splitwise/
├── README.md                  ← this file
├── DEPLOYMENT.md              ← step-by-step Render + Vercel guide
├── docker-compose.yml         ← local Postgres + backend + frontend
├── render.yaml                ← Render blueprint (web service + DB)
├── backend/
│   ├── Dockerfile             ← multi-stage Maven → JRE
│   ├── pom.xml
│   └── src/main/java/com/shashi/splitwise/
│       ├── auth/              ← register, login, JWT issue
│       ├── user/              ← User entity + repo
│       ├── group/             ← groups + members + invitations
│       ├── expense/           ← expenses + SplitCalculator
│       ├── balance/           ← per-user net balance
│       ├── settlement/        ← DebtSimplifier + persisted settlements
│       └── common/            ← config, error, security, web
├── frontend/
│   ├── Dockerfile             ← multi-stage Vite → nginx
│   ├── nginx.conf             ← SPA fallback + cache headers
│   ├── vercel.json            ← Vercel project config
│   └── src/
│       ├── api/               ← typed axios wrappers per resource
│       ├── auth/              ← Login/Register pages, RequireAuth
│       ├── components/        ← reusable UI primitives
│       ├── features/          ← groups · expenses · balances · settlements
│       ├── hooks/             ← (small)
│       ├── lib/               ← currency, dates, classnames, query client
│       ├── routes/            ← React Router config
│       └── store/             ← Zustand auth store
└── docs/
    ├── RESUME.md              ← three resume-bullet variants
    ├── INTERVIEW.md           ← 15+ likely Q&As grouped by theme
    └── LINKEDIN.md            ← short announcement post
```

---

## Acknowledgements

Inspired by the original [Splitwise](https://www.splitwise.com/). The
debt-simplification approach is the standard greedy
"largest-creditor-meets-largest-debtor" algorithm — well-known, but the
implementation here pays unusual attention to determinism and integer
arithmetic so it's safe to ship.
