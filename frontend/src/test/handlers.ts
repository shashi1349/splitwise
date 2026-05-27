import { http, HttpResponse } from 'msw';

const BASE = 'http://localhost:8080';

/**
 * Default MSW handlers for tests. Tests can override individual routes
 * via {@code server.use(...)} for failure-path scenarios.
 */
export const handlers = [
  http.post(`${BASE}/auth/login`, async () =>
    HttpResponse.json({
      token: 'test-jwt',
      expiresInSeconds: 7200,
      user: { id: 1, email: 'alice@example.com', displayName: 'Alice' },
    }),
  ),
  http.post(`${BASE}/auth/register`, async () =>
    HttpResponse.json(
      {
        token: 'test-jwt',
        expiresInSeconds: 7200,
        user: { id: 2, email: 'new@example.com', displayName: 'New User' },
      },
      { status: 201 },
    ),
  ),
  http.get(`${BASE}/groups/:groupId/balances`, () => HttpResponse.json([])),
];
