// Renders the FoodOS menu page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute } from "@tanstack/react-router";
import { useQueries, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Search } from "lucide-react";
import { restaurantService } from "@/services/restaurantService";
import { menuService } from "@/services/menuService";
import { errorMessage } from "@/services/api";
import { pageMeta } from "@/lib/meta";
import { MenuItemCard } from "@/components/food/Cards";
import { EmptyState, ErrorState, Loading, PageHeader } from "@/components/common/States";

export const Route = createFileRoute("/menu")({
  validateSearch: (s: Record<string, unknown>): { q?: string } => ({ q: typeof s.q === "string" ? s.q : undefined }),
  head: () => pageMeta("Menu", "Search every dish across FoodOS restaurants."),
  component: MenuPage,
});

// No "all menu items" route exists, so we combine /restaurants/{id}/menu for each restaurant.
function MenuPage() {
  const { q: initial } = Route.useSearch();
  const [q, setQ] = useState(initial ?? "");
  const rq = useQuery({ queryKey: ["restaurants"], queryFn: restaurantService.list });
  const menus = useQueries({
    queries: (rq.data ?? []).map((r) => ({ queryKey: ["menu", r.id], queryFn: () => menuService.byRestaurant(r.id) })),
  });

  if (rq.isLoading || menus.some((m) => m.isLoading)) return <Loading />;
  const err = rq.error ?? menus.find((m) => m.error)?.error;
  if (err) return <div className="px-4 py-16"><ErrorState message={errorMessage(err)} onRetry={() => rq.refetch()} /></div>;

  const t = q.toLowerCase();
  const all = (rq.data ?? []).flatMap((r, i) => (menus[i]?.data ?? []).map((item) => ({ item, r })));
  const list = all.filter(({ item, r }) => !t || [item.name, item.category, item.description, r.name, r.cuisine].some((f) => f?.toLowerCase().includes(t.replace(/s$/, ""))));

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <PageHeader title="Menu" subtitle={`${list.length} dishes${q ? ` matching “${q}”` : ""}`} />
      <div className="mb-8 flex items-center gap-2 rounded-full border bg-card px-4 py-2 shadow-soft">
        <Search className="h-4 w-4 text-muted-foreground" />
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search dishes or restaurants" className="min-w-0 flex-1 bg-transparent outline-none" />
      </div>
      {list.length === 0 ? <EmptyState emoji="🥲" title="Nothing matches" text="Try another dish name." /> : (
        <div className="grid gap-4 md:grid-cols-2">{list.map(({ item, r }) => <MenuItemCard key={item.id} item={item} restaurant={r} />)}</div>
      )}
    </div>
  );
}
