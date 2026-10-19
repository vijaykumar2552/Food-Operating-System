// Renders the FoodOS restaurants $id page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, Bike, Clock, MapPin, Star } from "lucide-react";
import { restaurantService } from "@/services/restaurantService";
import { menuService } from "@/services/menuService";
import { errorMessage } from "@/services/api";
import { foodEmoji, formatPrice } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { Sticker } from "@/components/food/Sticker";
import { MenuItemCard } from "@/components/food/Cards";
import { EmptyState, ErrorState, Loading } from "@/components/common/States";

export const Route = createFileRoute("/restaurants/$id")({
  head: () => pageMeta("Restaurant menu", "See the full menu and add dishes to your FoodOS cart."),
  component: RestaurantMenu,
});

function RestaurantMenu() {
  const id = Number(Route.useParams().id);
  const rq = useQuery({ queryKey: ["restaurant", id], queryFn: () => restaurantService.get(id) });
  const mq = useQuery({ queryKey: ["menu", id], queryFn: () => menuService.byRestaurant(id) });
  const r = rq.data;

  if (rq.isLoading) return <Loading />;
  if (rq.error || !r) return <div className="px-4 py-16"><ErrorState message={errorMessage(rq.error)} onRetry={() => rq.refetch()} /></div>;

  const groups = new Map<string, typeof mq.data>();
  (mq.data ?? []).forEach((m) => {
    const k = m.category || "Menu";
    groups.set(k, [...(groups.get(k) ?? []), m]);
  });

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <Link to="/restaurants" className="inline-flex items-center gap-1 text-sm font-medium text-muted-foreground hover:text-foreground"><ArrowLeft className="h-4 w-4" /> All restaurants</Link>
      <div className="bg-hero mt-4 flex flex-col gap-6 rounded-3xl border bg-card p-6 shadow-soft md:flex-row md:items-center">
        <Sticker emoji={foodEmoji(r.cuisine, r.name)} seed={r.id} size="lg" />
        <div className="flex-1">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-3xl font-extrabold md:text-4xl">{r.name}</h1>
            {r.isOpen !== undefined && (
              <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${r.isOpen ? "bg-success/15 text-success" : "bg-muted text-muted-foreground"}`}>{r.isOpen ? "Open" : "Closed"}</span>
            )}
          </div>
          {r.cuisine && <p className="font-medium text-accent-foreground">{r.cuisine}</p>}
          {r.description && <p className="mt-2 text-muted-foreground">{r.description}</p>}
          <div className="mt-4 flex flex-wrap gap-4 text-sm">
            {r.rating !== undefined && <span className="flex items-center gap-1 font-semibold"><Star className="h-4 w-4 fill-warning text-warning" /> {r.rating.toFixed(1)}</span>}
            {r.deliveryTimeMinutes !== undefined && <span className="flex items-center gap-1"><Clock className="h-4 w-4" /> {r.deliveryTimeMinutes} min</span>}
            {r.deliveryCharge !== undefined && <span className="flex items-center gap-1"><Bike className="h-4 w-4" /> {r.deliveryCharge === 0 ? "Free delivery" : formatPrice(r.deliveryCharge)}</span>}
            {r.location && <span className="flex items-center gap-1"><MapPin className="h-4 w-4" /> {r.location}</span>}
          </div>
        </div>
      </div>

      <div className="mt-10">
        {mq.isLoading ? <Loading label="Loading menu…" /> : mq.error ? <ErrorState message={errorMessage(mq.error)} onRetry={() => mq.refetch()} /> : groups.size === 0 ? (
          <EmptyState emoji="🍽️" title="No menu items yet" />
        ) : (
          [...groups.entries()].map(([cat, items]) => (
            <section key={cat} className="mb-10">
              <h2 className="mb-4 text-xl font-bold">{cat}</h2>
              <div className="grid gap-4 md:grid-cols-2">{items!.map((m) => <MenuItemCard key={m.id} item={m} restaurant={r} />)}</div>
            </section>
          ))
        )}
      </div>
    </div>
  );
}
