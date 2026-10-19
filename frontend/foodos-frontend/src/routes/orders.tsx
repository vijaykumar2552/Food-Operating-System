// Renders the FoodOS orders page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { orderService } from "@/services/orderService";
import { errorMessage } from "@/services/api";
import { formatPrice } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { RequireRole } from "@/components/common/RequireRole";
import { EmptyState, ErrorState, Loading, PageHeader, StatusBadge } from "@/components/common/States";

export const Route = createFileRoute("/orders")({
  head: () => pageMeta("My orders", "Track your FoodOS orders and their status."),
  component: () => <RequireRole role="CUSTOMER"><MyOrders /></RequireRole>,
});

function MyOrders() {
  const q = useQuery({ queryKey: ["orders", "my"], queryFn: orderService.my });
  return (
    <div className="mx-auto max-w-4xl px-4 py-10">
      <PageHeader title="My orders" />
      {q.isLoading ? <Loading /> : q.error ? <ErrorState message={errorMessage(q.error)} onRetry={() => q.refetch()} /> : !q.data?.length ? (
        <EmptyState emoji="🧾" title="No orders yet" action={<Button asChild className="rounded-full"><Link to="/restaurants">Order something</Link></Button>} />
      ) : (
        <div className="space-y-4">
          {q.data.map((o) => (
            <div key={o.id} className="rounded-3xl border bg-card p-5 shadow-soft">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <p className="font-bold">Order #{o.id}{o.restaurantName && <span className="font-medium text-muted-foreground"> · {o.restaurantName}</span>}</p>
                  {o.orderDate && <p className="text-xs text-muted-foreground">{new Date(o.orderDate).toLocaleString()}</p>}
                </div>
                <StatusBadge status={o.status} />
              </div>
              {o.items && (
                <ul className="mt-3 text-sm text-muted-foreground">
                  {o.items.map((i) => <li key={i.menuItemId}>{i.quantity} × {i.name ?? `Item #${i.menuItemId}`}</li>)}
                </ul>
              )}
              <div className="mt-3 flex flex-wrap items-center justify-between border-t pt-3 text-sm">
                <span className="text-muted-foreground">{[o.paymentMethod, o.paymentStatus].filter(Boolean).join(" · ")}</span>
                <span className="font-bold">{formatPrice(o.totalAmount)}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
