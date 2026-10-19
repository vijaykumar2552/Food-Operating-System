// Renders the FoodOS admin index page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { restaurantService } from "@/services/restaurantService";
import { orderService } from "@/services/orderService";
import { pageMeta } from "@/lib/meta";
import { PageHeader } from "@/components/common/States";

export const Route = createFileRoute("/admin/")({
  head: () => pageMeta("Admin dashboard", "FoodOS admin overview."),
  component: Dashboard,
});

function Stat({ emoji, label, value, to }: { emoji: string; label: string; value: number | undefined; to: "/admin/restaurants" | "/admin/orders" | "/admin/menu" }) {
  return (
    <Link to={to} className="card-hover rounded-3xl border bg-card p-6 shadow-soft">
      <span className="sticker text-4xl">{emoji}</span>
      <p className="mt-4 text-sm text-muted-foreground">{label}</p>
      <p className="font-display text-4xl font-extrabold">{value ?? <span className="text-base font-medium text-muted-foreground">Unavailable</span>}</p>
    </Link>
  );
}

function Dashboard() {
  const r = useQuery({ queryKey: ["admin", "restaurants"], queryFn: restaurantService.adminList });
  const o = useQuery({ queryKey: ["admin", "orders"], queryFn: orderService.adminList });
  // "Pending" = anything not delivered/cancelled. UNVERIFIED against backend status values.
  const pending = o.data?.filter((x) => !["DELIVERED", "CANCELLED"].includes(x.status)).length;
  return (
    <>
      <PageHeader title="Dashboard" subtitle="Figures come straight from the backend. If a request fails, it shows as unavailable." />
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        <Stat emoji="🏪" label="Restaurants" value={r.data?.length} to="/admin/restaurants" />
        <Stat emoji="🧾" label="Total orders" value={o.data?.length} to="/admin/orders" />
        <Stat emoji="⏳" label="Pending orders" value={pending} to="/admin/orders" />
      </div>
      <p className="mt-6 text-sm text-muted-foreground">Menu item count is not shown because there is no single endpoint for it. <Link to="/admin/menu" className="font-semibold text-primary">Manage menus →</Link></p>
    </>
  );
}
