// Renders the FoodOS admin orders page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { ChevronDown } from "lucide-react";
import { toast } from "sonner";
import { ORDER_STATUSES } from "@/types";
import { orderService } from "@/services/orderService";
import { errorMessage } from "@/services/api";
import { formatPrice } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { EmptyState, ErrorState, Loading, PageHeader, StatusBadge } from "@/components/common/States";

export const Route = createFileRoute("/admin/orders")({
  head: () => pageMeta("Order management", "View FoodOS orders and update their status."),
  component: AdminOrders,
});

function AdminOrders() {
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ["admin", "orders"], queryFn: orderService.adminList });
  const [openId, setOpenId] = useState<number | null>(null);
  const upd = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) => orderService.updateStatus(id, status),
    onSuccess: (_d, v) => { toast.success(`Order #${v.id} → ${v.status.replace(/_/g, " ")}`); qc.invalidateQueries({ queryKey: ["admin", "orders"] }); },
    onError: (e) => toast.error(errorMessage(e)),
  });

  return (
    <>
      <PageHeader title="Orders" subtitle="Update order status as they progress." />
      {q.isLoading ? <Loading /> : q.error ? <ErrorState message={errorMessage(q.error)} onRetry={() => q.refetch()} /> : !q.data?.length ? <EmptyState emoji="🧾" title="No orders yet" /> : (
        <div className="space-y-3">
          {q.data.map((o) => (
            <div key={o.id} className="rounded-3xl border bg-card p-4 shadow-soft">
              <div className="flex flex-wrap items-center gap-3">
                <button onClick={() => setOpenId(openId === o.id ? null : o.id)} className="flex flex-1 items-center gap-2 text-left">
                  <ChevronDown className={`h-4 w-4 transition-transform ${openId === o.id ? "rotate-180" : ""}`} />
                  <span className="font-bold">#{o.id}</span>
                  <span className="text-sm text-muted-foreground">{[o.customerName, o.restaurantName].filter(Boolean).join(" · ")}</span>
                </button>
                <span className="font-bold">{formatPrice(o.totalAmount)}</span>
                <StatusBadge status={o.status} />
                <select aria-label="Update status" value={o.status} disabled={upd.isPending} onChange={(e) => upd.mutate({ id: o.id, status: e.target.value })} className="rounded-full border bg-background px-3 py-1.5 text-sm">
                  {[...new Set([o.status, ...ORDER_STATUSES])].map((s) => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                </select>
              </div>
              {openId === o.id && (
                <div className="mt-3 border-t pt-3 text-sm">
                  {o.orderDate && <p className="text-muted-foreground">{new Date(o.orderDate).toLocaleString()}</p>}
                  <ul className="mt-2">{o.items?.map((i) => <li key={i.menuItemId}>{i.quantity} × {i.name ?? `Item #${i.menuItemId}`}{i.price !== undefined && ` — ${formatPrice(i.price)}`}</li>)}</ul>
                  <p className="mt-2 text-muted-foreground">{[o.paymentMethod, o.paymentStatus].filter(Boolean).join(" · ")}</p>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </>
  );
}
