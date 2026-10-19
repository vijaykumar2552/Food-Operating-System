// Renders the FoodOS admin menu page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Pencil, Plus } from "lucide-react";
import { toast } from "sonner";
import type { MenuItem } from "@/types";
import { restaurantService } from "@/services/restaurantService";
import { menuService, type MenuItemInput } from "@/services/menuService";
import { errorMessage } from "@/services/api";
import { foodEmoji, formatPrice } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Field } from "@/components/common/AuthLayout";
import { ConfirmDelete } from "@/components/common/ConfirmDelete";
import { EmptyState, ErrorState, Loading, PageHeader } from "@/components/common/States";

export const Route = createFileRoute("/admin/menu")({
  head: () => pageMeta("Menu management", "Manage menu items for each FoodOS restaurant."),
  component: AdminMenu,
});

function AdminMenu() {
  const qc = useQueryClient();
  const rq = useQuery({ queryKey: ["admin", "restaurants"], queryFn: restaurantService.adminList });
  // rid 0 = all restaurants (same combined view as the customer Menu page).
  const [rid, setRid] = useState<number>(0);
  // Same query keys + endpoint (/restaurants/{id}/menu) as the customer pages, so caches are shared.
  const menus = useQueries({
    queries: (rq.data ?? []).map((r) => ({ queryKey: ["menu", r.id], queryFn: () => menuService.adminList(r.id) })),
  });
  const categories = useQuery({ queryKey: ["admin", "categories"], queryFn: menuService.categories });
  const shown = (rq.data ?? []).filter((r) => rid === 0 || r.id === rid);
  const mq = {
    isLoading: menus.some((m) => m.isLoading),
    error: menus.find((m) => m.error)?.error,
    refetch: () => menus.forEach((m) => m.refetch()),
    data: (rq.data ?? []).flatMap((r, i) => (shown.includes(r) ? menus[i]?.data ?? [] : [])),
  };
  const nameOf = (id: number) => rq.data?.find((r) => r.id === id)?.name;
  const [editing, setEditing] = useState<MenuItem | null>(null);
  const [open, setOpen] = useState(false);
  // Only refetch the menus that changed, after the server confirmed.
  const refresh = (...ids: number[]) => {
    [...new Set(ids)].forEach((id) => qc.invalidateQueries({ queryKey: ["menu", id], exact: true }));
  };

  const del = useMutation({
    mutationFn: (m: MenuItem) => menuService.remove(m.id),
    onSuccess: (_d, m) => {
      toast.success("Item deleted");
      qc.setQueryData<MenuItem[]>(["menu", m.restaurantId], (old) => old?.filter((x) => x.id !== m.id));
      refresh(m.restaurantId);
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  if (rq.isLoading) return <Loading />;
  if (rq.error) return <ErrorState message={errorMessage(rq.error)} onRetry={() => rq.refetch()} />;
  if (!rq.data?.length) return <EmptyState emoji="🏪" title="Add a restaurant first" />;

  return (
    <>
      <PageHeader title="Menu" subtitle={mq.isLoading ? "Loading dishes…" : `${mq.data.length} dishes`}>
        <div className="flex gap-2">
          <select value={rid} onChange={(e) => setRid(Number(e.target.value))} className="rounded-full border bg-card px-4 py-2 text-sm font-medium">
            <option value={0}>All restaurants</option>
            {rq.data.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
          </select>
          <Button className="rounded-full" onClick={() => { setEditing(null); setOpen(true); }}><Plus className="h-4 w-4" /> Add item</Button>
        </div>
      </PageHeader>
      {mq.isLoading ? <Loading /> : mq.error ? <ErrorState message={errorMessage(mq.error)} onRetry={() => mq.refetch()} /> : !mq.data?.length ? <EmptyState emoji="🍽️" title="No items on this menu" /> : (
        <div className="overflow-hidden rounded-3xl border bg-card shadow-soft">
          {mq.data.map((m) => (
            <div key={m.id} className="flex items-center gap-4 border-b p-4 last:border-0">
              <span className="sticker text-3xl">{foodEmoji(m.name, m.category)}</span>
              <div className="min-w-0 flex-1">
                <p className="font-semibold">{m.name}</p>
                <p className="truncate text-sm text-muted-foreground">{[rid === 0 ? nameOf(m.restaurantId) : null, m.category, m.description].filter(Boolean).join(" · ")}</p>
              </div>
              <span className="font-bold">{formatPrice(m.price)}</span>
              <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${m.available ? "bg-success/15 text-success" : "bg-muted text-muted-foreground"}`}>{m.available ? "Available" : "Unavailable"}</span>
              <Button variant="ghost" size="icon" aria-label="Edit" onClick={() => { setEditing(m); setOpen(true); }}><Pencil className="h-4 w-4" /></Button>
              <ConfirmDelete name={m.name} pending={del.isPending && del.variables?.id === m.id} onConfirm={() => del.mutate(m)} />
            </div>
          ))}
        </div>
      )}
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{editing ? "Edit item" : "Add item"}</DialogTitle></DialogHeader>
          {open && <ItemForm restaurants={rq.data} categories={categories.data ?? []} restaurantId={editing?.restaurantId ?? (rid || rq.data[0].id)} initial={editing} onDone={() => {
            setOpen(false);
            refresh(editing?.restaurantId ?? (rid || rq.data[0].id));
          }} />}
        </DialogContent>
      </Dialog>
    </>
  );
}

function ItemForm({ restaurants, categories, restaurantId, initial, onDone }: { restaurants: { id: number; name: string }[]; categories: { id: number; restaurantId: number; name: string }[]; restaurantId: number; initial: MenuItem | null; onDone: () => void }) {
  const restaurantCategories = categories.filter((category) => category.restaurantId === restaurantId);
  const [f, setF] = useState<MenuItemInput>(initial ? { restaurantId: initial.restaurantId, categoryId: initial.categoryId, name: initial.name, description: initial.description, price: initial.price, category: initial.category, availability: initial.availability, available: initial.available } : { restaurantId, categoryId: restaurantCategories[0]?.id, name: "", description: "", price: 0, category: "", availability: "AVAILABLE", available: true });
  const [err, setErr] = useState("");
  const save = useMutation({
    mutationFn: async (): Promise<void> => {
      if (!f.categoryId) throw new Error("Select a category.");
      const payload = { categoryId: f.categoryId, name: f.name, description: f.description, price: f.price, availability: f.availability ?? (f.available ? "AVAILABLE" as const : "SOLD_OUT" as const) };
      if (initial) await menuService.update(initial.id, payload);
      else await menuService.create(payload);
    },
    onSuccess: () => {
      toast.success(initial ? "Item updated" : "Item added");
      onDone();
    },
    onError: (e) => setErr(errorMessage(e)),
  });
  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!f.name.trim()) return setErr("Name is required.");
    if (!(f.price > 0)) return setErr("Price must be greater than 0.");
    if (save.isPending) return; // block double submits
    setErr("");
    save.mutate();
  };
  return (
    <form onSubmit={submit} className="space-y-3">
      <label className="block text-sm font-medium">Restaurant
        <select value={f.restaurantId} onChange={(e) => setF({ ...f, restaurantId: Number(e.target.value) })} className="mt-1 w-full rounded-xl border bg-card px-3 py-2">
          {restaurants.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
      </label>
      <Field label="Name" value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} />
      <div className="grid grid-cols-2 gap-3">
        <Field label="Price (₹)" type="number" min={0} step="0.01" value={f.price} onChange={(e) => setF({ ...f, price: Number(e.target.value) })} />
        <label className="block text-sm font-medium">Category
          <select value={f.categoryId ?? ""} onChange={(e) => setF({ ...f, categoryId: Number(e.target.value) })} className="mt-1 w-full rounded-xl border bg-card px-3 py-2">
            <option value="">Select a category</option>
            {restaurantCategories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
          </select>
        </label>
      </div>
      <Field label="Description" value={f.description ?? ""} onChange={(e) => setF({ ...f, description: e.target.value })} />
      <label className="flex items-center gap-2 text-sm font-medium"><input type="checkbox" className="accent-primary" checked={f.availability === "AVAILABLE"} onChange={(e) => setF({ ...f, available: e.target.checked, availability: e.target.checked ? "AVAILABLE" : "SOLD_OUT" })} /> Available</label>
      {err && <p className="text-sm text-destructive">{err}</p>}
      <Button type="submit" className="w-full rounded-full" disabled={save.isPending}>{save.isPending ? "Saving…" : "Save"}</Button>
    </form>
  );
}
