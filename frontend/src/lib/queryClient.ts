import { QueryClient } from '@tanstack/react-query';

/**
 * Shared TanStack Query client. `staleTime` of 30s avoids redundant
 * refetches when navigating between sibling tabs in the same session.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});
