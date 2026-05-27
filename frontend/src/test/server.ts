import { setupServer } from 'msw/node';
import { handlers } from './handlers';

/**
 * Lifecycle:
 *   beforeAll  → server.listen()
 *   afterEach  → server.resetHandlers()
 *   afterAll   → server.close()
 */
export const server = setupServer(...handlers);
