// Renders the FoodOS restaurants index page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import { restaurantService } from "@/services/restaurantService";
import { errorMessage } from "@/services/api";
import { pageMeta } from "@/lib/meta";
import { RestaurantCard } from "@/components/food/Cards";
import { EmptyState, ErrorState, Loading, PageHeader } from "@/components/common/States";

export const Route = createFileRoute("/restaurants/")({
  head: () => pageMeta("Restaurants", "Browse restaurants on FoodOS by cuisine, location and availability."),
  component: Restaurants,
});

function Restaurants() {
  const q = useQuery({ queryKey: ["restaurants"], queryFn: restaurantService.list });
  const [text, setText] = useState("");
  const [cuisine, setCuisine] = useState("All");
  const [openOnly, setOpenOnly] = useState(false);

  const cuisines = useMemo(() => ["All", ...new Set((q.data ?? []).map((r) => r.cuisine).filter(Boolean) as string[])], [q.data]);
  const list = (q.data ?? []).filter((r) => {
    const t = text.toLowerCase();
    const matchText = !t || [r.name, r.cuisine, r.location, r.description].some((f) => f?.toLowerCase().includes(t));
    return matchText && (cuisine === "All" || r.cuisine === cuisine) && (!openOnly || r.isOpen === true);
  });

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <PageHeader title="Restaurants" subtitle="Pick a place, then pick your plate." />
      <div className="mb-6 flex flex-wrap items-center gap-3">
        <div className="flex flex-1 items-center gap-2 rounded-full border bg-card px-4 py-2 shadow-soft">
          <Search className="h-4 w-4 text-muted-foreground" />
          <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Search name, cuisine or location" className="min-w-0 flex-1 bg-transparent outline-none" />
        </div>
        <label className="flex items-center gap-2 text-sm font-medium">
          <input type="checkbox" checked={openOnly} onChange={(e) => setOpenOnly(e.target.checked)} className="accent-primary" /> Open now
        </label>
      </div>
      <div className="mb-8 flex flex-wrap gap-2">
        {cuisines.map((c) => (
          <button key={c} onClick={() => setCuisine(c)} className={`rounded-full border px-4 py-1.5 text-sm font-medium transition-colors ${cuisine === c ? "border-primary bg-primary text-primary-foreground" : "bg-card hover:bg-secondary"}`}>
            {c}
          </button>
        ))}
      </div>
      {q.isLoading ? <Loading /> : q.error ? <ErrorState message={errorMessage(q.error)} onRetry={() => q.refetch()} /> : list.length === 0 ? (
        <EmptyState emoji="🔍" title="No restaurants found" text="Try a different search or filter." />
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{list.map((r) => <RestaurantCard key={r.id} r={r} />)}</div>
      )}
    </div>
  );
}
