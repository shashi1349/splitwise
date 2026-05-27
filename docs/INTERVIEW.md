# Splitwise-Lite — interview talking points

Likely questions grouped by theme, with crisp first-pass answers.
Memorise the algorithm sketch and the trade-off explanations; the rest
will fall out of "I built it, here's where it lives in the repo".

---

## Algorithms & data modelling

### 1. Walk me through the debt-simplification algorithm.

Build a max-heap of creditors keyed on `+amount` and another of debtors
keyed on `−amount` (so largest first by absolute value). While both
heaps are non-empty: pop the largest creditor `c` and largest debtor
`d`, transfer `min(c, d)` from debtor to creditor, push back any
leftover. Whichever side fully settles is removed permanently.

Each iteration removes ≥ 1 party from a heap, so after at most `N − 1`
iterations only one party can remain — and since the global sum is
zero, that one is also at zero. Time `O(N log N)` because each push and
each pop is `O(log N)` and we do at most `N` pushes plus `N` pops total.

### 2. Is this algorithm optimal?

It's the **minimum-transfer** solution under the natural Splitwise model
where any debtor can pay any creditor. The general
"minimum-number-of-transactions" problem on an arbitrary directed graph
is **NP-hard** (it generalises subset-sum), but our model collapses
every pairwise debt into a single per-user net balance first, which is
exactly what makes greedy optimal.

### 3. Why does the SplitCalculator return ≤ N − 1 transfers and not exactly N − 1?

Because some balances might already be zero. Picture A=+100, B=0,
C=−100: only one transfer is needed (A ↔ C), and B is never visited.

### 4. Walk me through SplitCalculator's PERCENT branch.

Each percent comes in as `BigDecimal` with at most 2 decimal places.
I convert each to integer hundredths-of-percent (`33.33% → 3333`),
verify the sum equals exactly `10000`, then compute every share as
`floor(totalCents × hundredths / 10000)`. The floor guarantees a
deficit ≤ N − 1 cents, so I sprinkle the remainder one cent at a time
to participants in **sorted-userId order**. That makes two clients
computing the same split agree byte-for-byte.

### 5. How do you avoid floating-point drift?

Three lines of defence: (a) HTTP boundary uses `BigDecimal` validated
with `@Digits(integer=12, fraction=2)`; (b) the moment a value enters
the service layer it becomes integer cents via
`bd.movePointRight(2).longValueExact()` — `longValueExact` throws on
extra decimal places; (c) every internal arithmetic op uses `Math.addExact`
or `Math.multiplyExact` to surface overflow. The frontend mirrors this
with `parseAmountToCents` / `centsToDecimalString` and never touches
`parseFloat`.

---

## Spring Boot & the JVM

### 6. Why do you need a `JwtAuthenticationFilter` instead of `addFilter` on Spring Security's `OAuth2ResourceServer`?

`OAuth2ResourceServer` is great when an external IdP signs your tokens
and you only need to verify them against a JWK set. Here, the same
service issues *and* verifies — there's no IdP, no JWK rotation, no
audience claim to honour. A custom filter is a couple of dozen lines
that I fully control. If I ever add Google sign-in I'd switch this layer.

### 7. Order of filters in your security chain — why matters?

`CorrelationIdFilter` runs at `Ordered.HIGHEST_PRECEDENCE` so it stamps
the MDC before *anything* else logs. Then Spring Security's chain runs;
inside it I `addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`
so the JWT principal is in the SecurityContext before any controller
adapter tries to resolve `@AuthenticationPrincipal`. The
`AuthenticationEntryPoint` and `AccessDeniedHandler` are wired to write
ProblemDetail JSON instead of Spring's default HTML error page.

### 8. Why does `requireMembership` throw 404 and not 403?

To prevent enumeration of group ids by an attacker with a valid JWT.
Members already have the full list at `GET /groups`, so 404 doesn't
hurt their UX, but it stops a third party from probing
`GET /groups/{id}` to map the system.

### 9. Why is `BalanceService.computeBalances` `@Transactional(readOnly=true)`?

Two reasons: it tells Hibernate it can skip dirty-checking on detach
(faster, fewer surprises), and it tells the JDBC layer to use a
read-only connection if the connection pool is configured to issue them.
On Postgres I'd also gain query-replica routing if I ever add one.

### 10. Why an `@EntityGraph` on `findPageByGroupId` instead of `JOIN FETCH`?

`JOIN FETCH` and pagination don't compose: Hibernate refuses to paginate
a fetched collection because the row count after the JOIN no longer
maps to entity count. `@EntityGraph` produces the same eager-loading
effect but lets Hibernate keep the count query and the data query
separate, so pagination is correct.

---

## React, TypeScript, and the frontend

### 11. Why TanStack Query *and* Zustand? Aren't they the same thing?

Server state and client state are different problems. TanStack Query
owns *what the server thinks*: caching, revalidation, mutation, retry,
"keep showing the previous page while the next one loads". Zustand owns
*what only the browser knows*: the JWT, the current user's display
name, the dark-mode preference. Mixing them forces you to reinvent the
other half. `react-redux` could play the second role too, but Zustand
is ~1 KB and persists to localStorage with three lines.

### 12. Walk me through the Add Expense form's live validation.

The form holds `description`, `amount`, `payerId`, `splitType`, and a
`shares: ShareRow[]` array (one row per group member). On every render
I call `computeSplitSummary(splitType, totalCents, currency, shares)`,
which is a pure function returning `{ ok, message }`. The message
mirrors the server's `SplitCalculator` error messages exactly
("Shares sum to ₹950 — ₹50 short."). The submit button is disabled
unless `ok` is true. The same pure function is unit-tested in seven
cases without mounting React, so a regression in the message wording
trips a test.

### 13. Why integer cents on the frontend too — isn't that overkill?

Browsers' `Number` is IEEE-754 double, which loses precision past
`2^53`. That's plenty for a single expense, but accumulating a trip's
worth of partial cents through `parseFloat("33.33") * 3` produces
`99.99000000000001`. Keeping cents as `number` (still a double, but
holding integers ≤ `2^53`) sidesteps it. The discipline cost is one
helper at the boundary — small.

### 14. How does the frontend stay accessible?

Every input has a real `<label htmlFor>`. Buttons use `Headless UI` for
focus-trap correctness in dialogs. The Balances bar has an `aria-label`
that mirrors the English status line so screen readers don't lose
information. ESLint's `react-hooks` plugin keeps effects honest. WCAG-AA
contrast is hand-checked in the colour palette (`brand-600` on white
and on `slate-950`).

---

## Trade-offs and "what I'd do differently"

### 15. Why integer cents end-to-end instead of using something like Joda Money?

Joda Money is great in heavyweight apps where you also need allocation,
formatting, and currency conversion. Here, I needed three operations
(add, subtract, multiply by an integer percent) and a single currency
per group. The cost of carrying around `Money` objects across JSON,
JPA, and React would have outweighed the win. If multi-currency-per-
expense ever lands (see `What I'd build next`), I'd revisit.

### 16. Why no microservices?

The whole API is ~3 000 LoC. The bottleneck is going to be product
features, not throughput. A single boot-jar fits on Render's free tier;
splitting it would multiply ops cost without helping anyone. The
package-by-feature layout means I *could* lift `expense/` or
`settlement/` into their own service later, but I'd want to see real
load first.

### 17. What would you change about the schema?

Two things. First, `expenses.currency_code` is redundant with
`expense_groups.currency_code` today (we always inherit). I'd drop it
once multi-currency-per-expense is on the roadmap and the column has a
real reason to exist. Second, I'd add a `users.last_login_at` column —
useful for dormant-account analytics and a 1-line addition.

### 18. Where would horizontal scaling break first?

The JWT path is stateless so the API itself scales fine. The first
contention point is the single-DB write path on `POST /expenses` if
two members of the same group POST simultaneously (Postgres handles
it but their tabs need to refetch). The next is the in-memory rate
limiter — there isn't one yet; I'd add Bucket4j with a Redis backend
the day before launch.

### 19. Tell me about the test that gives you the most confidence.

`SplitCalculatorTest.equal_invariantSumProperty` — a parameterised test
across six `(totalCents, n)` pairs that asserts `Σshares = totalCents`
and every share is non-negative. It's not a property-based fuzz, but
it's a property assertion that would fail loudly if anyone refactored
the remainder allocation incorrectly.

### 20. What was the trickiest bug you hit?

In Module 7, `useAuthStore.setState({ token: null, user: null }, true)`
in the test `afterEach` was nuking the store's *methods* (`setAuth`,
`clear`) along with the data, because the second arg to Zustand's
`setState` is `replace`. The next test that called `clear()` got
`is not a function`. Fix was three characters — drop the `, true` —
but the symptom (a 401 test failing because it couldn't surface a
ProblemDetail) was three layers away from the root cause.
