# Splitwise-Lite — Backend

Spring Boot 3.3 + Java 21 REST API.

## Run

```bash
# Dev profile (in-memory H2, default)
mvn spring-boot:run

# Prod profile (Postgres) — requires env vars from .env.example
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

## Test

```bash
mvn test
```

## Endpoints (so far)

- `GET /actuator/health` — liveness probe
- `GET /actuator/info`   — build metadata
- `GET /swagger-ui.html` — interactive API docs (added in Module 2)
