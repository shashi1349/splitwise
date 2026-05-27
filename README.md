# Splitwise-Lite

Group expense splitter with **debt simplification** — minimise the number
of settlement transfers among friends. Built module-by-module as a
production-quality fullstack mono-repo.

> **Status:** Module 1 — Bootstrap complete. Subsequent modules add auth,
> groups, expenses, balances, debt simplification, tests, and deployment.

## Tech stack

| Layer        | Tech                                                                |
| ------------ | ------------------------------------------------------------------- |
| **Backend**  | Java 21, Spring Boot 3.3, Spring Data JPA, Flyway, PostgreSQL, JWT  |
| **Frontend** | React 18 + TypeScript, Vite, Tailwind CSS, React Query, Zustand     |
| **Tests**    | JUnit 5 + Mockito + AssertJ; Vitest + React Testing Library + MSW   |
| **Deploy**   | Docker (multi-stage), Render (backend + Postgres), Vercel (frontend) |

## Repository layout

```
splitwise/
├── backend/        Spring Boot API
├── frontend/       React + Vite UI
├── docker-compose.yml
└── README.md
```

## Quick start (development)

Requires Java 21+, Maven 3.9+, Node 20+, Docker (optional).

```bash
# 1. Optional — local Postgres for prod-profile testing
docker compose up -d postgres

# 2. Backend (defaults to dev profile, in-memory H2)
cd backend
mvn spring-boot:run

# 3. Frontend
cd ../frontend
npm install
npm run dev
```

Backend: <http://localhost:8080>  ·  Frontend: <http://localhost:5173>

Full documentation, screenshots, and deployment guides arrive in Module 8.
