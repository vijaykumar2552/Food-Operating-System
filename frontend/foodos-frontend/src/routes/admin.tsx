// Renders the FoodOS admin page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Outlet } from "@tanstack/react-router";
import { RequireRole } from "@/components/common/RequireRole";

export const Route = createFileRoute("/admin")({
  component: () => (
    <RequireRole role="ADMIN">
      <div className="mx-auto max-w-6xl px-4 py-10">
        <Outlet />
      </div>
    </RequireRole>
  ),
});
