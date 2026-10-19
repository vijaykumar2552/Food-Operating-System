// Renders the FoodOS admin restaurants page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Pencil, Plus } from "lucide-react";
import { toast } from "sonner";
import type { Restaurant } from "@/types";
import { restaurantService, type RestaurantInput } from "@/services/restaurantService";
import { errorMessage } from "@/services/api";
import { foodEmoji, formatPrice } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Field } from "@/components/common/AuthLayout";
import { ConfirmDelete } from "@/components/common/ConfirmDelete";
import { EmptyState, ErrorState, Loading, PageHeader } from "@/components/common/States";

export const Route = createFileRoute("/admin/restaurants")({
  head: () => pageMeta("Restaurant management", "Add, edit and remove FoodOS restaurants."),
  component: AdminRestaurants,
});

const EMPTY: RestaurantInput = { name: "", cuisine: "", description: "", location: "", deliveryCharge: 0, deliveryTimeMinutes: 30, isOpen: true };

function AdminRestaurants() {
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ["admin", "restaurants"], queryFn: restaurantService.adminList });
  const [editing, setEditing] = useState<Restaurant | null>(null);
  const [open, setOpen] = useState(false);
  const refresh = () => {
    qc.invalidateQueries({ queryKey: ["admin"] });
    qc.invalidateQueries({ queryKey: ["restaurants"] });
  };

  const del = useMutation({
    mutationFn: (id: number) => restaurantService.remove(id),
    onSuccess: () => { toast.success("Restaurant deleted"); refresh(); },
    onError: (e) => toast.error(errorMessage(e)),
  });

  return (
    <>
      <PageHeader title="Restaurants" subtitle="Manage restaurants listed on FoodOS.">
        <Button className="rounded-full" onClick={() => { setEditing(null); setOpen(true); }}><Plus className="h-4 w-4" /> Add restaurant</Button>
      </PageHeader>
      {q.isLoading ? <Loading /> : q.error ? <ErrorState message={errorMessage(q.error)} onRetry={() => q.refetch()} /> : !q.data?.length ? <EmptyState emoji="🏪" title="No restaurants yet" /> : (
        <div className="overflow-hidden rounded-3xl border bg-card shadow-soft">
          {q.data.map((r) => (
            <div key={r.id} className="flex items-center gap-4 border-b p-4 last:border-0">
              <span className="sticker text-3xl">{foodEmoji(r.cuisine, r.name)}</span>
              <div className="min-w-0 flex-1">
                <p className="font-semibold">{r.name}</p>
                <p className="truncate text-sm text-muted-foreground">{[r.cuisine, r.location, r.deliveryCharge !== undefined && `Delivery ${formatPrice(r.deliveryCharge)}`].filter(Boolean).join(" · ")}</p>
              </div>
              {r.isOpen !== undefined && <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${r.isOpen ? "bg-success/15 text-success" : "bg-muted text-muted-foreground"}`}>{r.isOpen ? "Open" : "Closed"}</span>}
              <Button variant="ghost" size="icon" aria-label="Edit" onClick={() => { setEditing(r); setOpen(true); }}><Pencil className="h-4 w-4" /></Button>
              <ConfirmDelete name={r.name} onConfirm={() => del.mutate(r.id)} />
            </div>
          ))}
        </div>
      )}
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{editing ? "Edit restaurant" : "Add restaurant"}</DialogTitle></DialogHeader>
          {open && <RestaurantForm initial={editing} onDone={() => { setOpen(false); refresh(); }} />}
        </DialogContent>
      </Dialog>
    </>
  );
}

function RestaurantForm({ initial, onDone }: { initial: Restaurant | null; onDone: () => void }) {
  const [f, setF] = useState<RestaurantInput>(initial ? { ...initial } : EMPTY);
  const [err, setErr] = useState("");
  const save = useMutation({
    mutationFn: async (): Promise<void> => {
      if (initial) await restaurantService.update(initial.id, f);
      else await restaurantService.create(f);
    },
    onSuccess: () => { toast.success(initial ? "Restaurant updated" : "Restaurant added"); onDone(); },
    onError: (e) => setErr(errorMessage(e)),
  });
  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!f.name.trim()) return setErr("Name is required.");
    if ((f.deliveryCharge ?? 0) < 0) return setErr("Delivery charge cannot be negative.");
    setErr("");
    save.mutate();
  };
  return (
    <form onSubmit={submit} className="space-y-3">
      <Field label="Name" value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} />
      <div className="grid grid-cols-2 gap-3">
        <Field label="Cuisine" value={f.cuisine ?? ""} onChange={(e) => setF({ ...f, cuisine: e.target.value })} />
        <Field label="Location" value={f.location ?? ""} onChange={(e) => setF({ ...f, location: e.target.value })} />
        <Field label="Delivery charge (₹)" type="number" min={0} value={f.deliveryCharge ?? 0} onChange={(e) => setF({ ...f, deliveryCharge: Number(e.target.value) })} />
        <Field label="Delivery time (min)" type="number" min={0} value={f.deliveryTimeMinutes ?? 0} onChange={(e) => setF({ ...f, deliveryTimeMinutes: Number(e.target.value) })} />
      </div>
      <Field label="Description" value={f.description ?? ""} onChange={(e) => setF({ ...f, description: e.target.value })} />
      <label className="flex items-center gap-2 text-sm font-medium"><input type="checkbox" className="accent-primary" checked={!!f.isOpen} onChange={(e) => setF({ ...f, isOpen: e.target.checked })} /> Open for orders</label>
      {err && <p className="text-sm text-destructive">{err}</p>}
      <Button type="submit" className="w-full rounded-full" disabled={save.isPending}>{save.isPending ? "Saving…" : "Save"}</Button>
    </form>
  );
}
