// Creates the FoodOS TanStack Router and its shared React Query client.
// Router context carries query state while route navigation preserves cached data.

import { QueryClient } from "@tanstack/react-query";
import { createRouter } from "@tanstack/react-router";
import { routeTree } from "./routeTree.gen";

export const getRouter = () => {
  const queryClient = new QueryClient({
    // Reuse cached data for 30s when switching pages instead of refetching every
    // menu on every mount/focus. Mutations still refresh what they change.
    defaultOptions: { queries: { staleTime: 30_000, refetchOnWindowFocus: false, retry: 1 } },
  });

  const router = createRouter({
    routeTree,
    context: { queryClient },
    scrollRestoration: true,
    defaultPreloadStaleTime: 0,
  });

  return router;
};
