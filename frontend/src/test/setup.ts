import '@testing-library/jest-dom/vitest';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { server } from './server';
import { useAuthStore } from '@/store/authStore';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));

afterEach(() => {
  server.resetHandlers();
  localStorage.clear();
  // Reset only the persisted fields — passing replace=true would also
  // delete the action methods (setAuth, clear) that the components use.
  useAuthStore.setState({ token: null, user: null });
});

afterAll(() => server.close());
