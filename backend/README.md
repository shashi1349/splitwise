# Splitwise-Lite — Backend

Java 21 + Spring Boot 3.3.5 REST API. Package-by-feature, with a strict
`api → application → domain` layering inside each feature. All money
math in integer cents.

## Run

```bash
# Dev profile (in-memory H2, default)
mvn spring-boot:run

# Prod profile (Postgres) — set env vars from .env.example first
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

## Test

```bash
mvn test    # 38 unit tests, all green
```

The pom sets `byte-buddy.version=1.15.11` and adds
`-Dnet.bytebuddy.experimental=true` to the surefire `argLine` so Mockito
inline-mocking keeps working when the build host is on a JDK newer than
Byte Buddy officially supports. Bytecode is still emitted at level 21.

## Container

```bash
docker build -t splitwise-api .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  splitwise-api
```

The Dockerfile is multi-stage (Maven build → `eclipse-temurin:21-jre-alpine`
runtime, ~80 MB compressed) and runs as a non-root `app` user.

## Endpoint surface

See the [top-level README API table](../README.md#api-reference) or hit
`/swagger-ui.html` once the app is up. Every protected endpoint requires
`Authorization: Bearer <jwt>`.

## Configuration knobs

| Env var                       | Default                          | Effect |
| ----------------------------- | -------------------------------- | ------ |
| `SPRING_PROFILES_ACTIVE`      | `dev`                            | `dev` = H2 in PG mode, `prod` = Postgres. |
| `JWT_SECRET`                  | dev-only fallback                | HS256 signing key. **Must** be ≥ 32 ASCII bytes in production. |
| `APP_FRONTEND_ORIGIN`         | `http://localhost:5173`          | Single allowed CORS origin. |
| `SPRING_DATASOURCE_URL`       | constructed from DB_HOST/PORT/NAME | Full JDBC URL — overrides the constructed default. |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | —                  | Required in prod. |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `splitwise` | Used to build `SPRING_DATASOURCE_URL` when it isn't set explicitly. |

## Where the interesting code lives

- `expense/application/SplitCalculator.java` — pure functions for
  EQUAL / EXACT / PERCENT, with the sum-equals-total invariant.
- `settlement/application/DebtSimplifier.java` — two-max-heap greedy
  algorithm guaranteeing ≤ N − 1 transfers. ~80 LOC including
  Javadoc explaining correctness.
- `balance/application/BalanceService.java` — single-pass aggregation
  over expenses with settlement subtraction.
- `common/error/GlobalExceptionHandler.java` — every exception →
  RFC 7807 ProblemDetail.
- `common/security/JwtAuthenticationFilter.java` — Bearer parsing,
  filter chain entry, principal injection via `@AuthenticationPrincipal`.
