// Renders the FoodOS index page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { ArrowRight, Search } from "lucide-react";
import { restaurantService } from "@/services/restaurantService";
import { menuService } from "@/services/menuService";
import { errorMessage } from "@/services/api";
import { CATEGORIES } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { MenuItemCard, RestaurantCard } from "@/components/food/Cards";
import { ErrorState, Loading } from "@/components/common/States";

export const Route = createFileRoute("/")({
  head: () => pageMeta("Home", "Order from great local restaurants with FoodOS. Good food. Smarter choices. Cash on Delivery."),
  component: Home,
});

function Home() {
  const nav = useNavigate();
  const [q, setQ] = useState("");
  const rq = useQuery({ queryKey: ["restaurants"], queryFn: restaurantService.list });
  const featured = (rq.data ?? []).slice(0, 3);
  const first = featured[0];
  const popular = useQuery({
    queryKey: ["menu", first?.id],
    queryFn: () => menuService.byRestaurant(first!.id),
    enabled: !!first,
  });

  const search = (e: React.FormEvent) => {
    e.preventDefault();
    nav({ to: "/menu", search: { q } });
  };

  return (
    <div>
      <section className="bg-hero">
        <div className="mx-auto grid max-w-6xl items-center gap-10 px-4 py-16 md:grid-cols-2 md:py-24">
          <div>
            <span className="inline-flex rounded-full bg-accent px-3 py-1 text-xs font-semibold text-accent-foreground">Cash on Delivery · Fast & fresh</span>
            <h1 className="mt-5 text-5xl font-extrabold leading-[1.02] md:text-6xl">
              Good food.<br /><span className="text-primary">Smarter choices.</span>
            </h1>
            <p className="mt-4 max-w-md text-lg text-muted-foreground">Discover local restaurants, build your cart, and get it delivered to your door.</p>
            <form onSubmit={search} className="mt-8 flex max-w-md items-center gap-2 rounded-full border bg-card p-1.5 shadow-soft">
              <Search className="ml-3 h-5 w-5 text-muted-foreground" />
              <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search biryani, pizza, restaurants…" className="min-w-0 flex-1 bg-transparent py-2 outline-none placeholder:text-muted-foreground" />
              <Button type="submit" className="rounded-full">Search</Button>
            </form>
            <Button asChild variant="link" className="mt-4 px-0 text-base">
              <Link to="/restaurants">Explore Restaurants <ArrowRight className="h-4 w-4" /></Link>
            </Button>
          </div>
          <div className="relative mx-auto aspect-square w-full max-w-sm">
            <div className="absolute inset-6 rounded-[42%] bg-sticker-1 rotate-6" />
            <div className="absolute inset-16 rounded-[40%] bg-sticker-2 -rotate-6" />
            <span className="sticker float-slow absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 text-[9rem]">🍔</span>
            <span className="sticker float-slow absolute left-2 top-6 text-6xl [animation-delay:-1s]">🍕</span>
            <span className="sticker float-slow absolute right-0 top-10 text-6xl [animation-delay:-2s]">🍜</span>
            <span className="sticker float-slow absolute bottom-6 left-6 text-6xl [animation-delay:-3s]">🥤</span>
            <span className="sticker float-slow absolute bottom-2 right-8 text-6xl [animation-delay:-4s]">🍰</span>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-12">
        <h2 className="text-2xl font-bold">What are you craving?</h2>
        <div className="mt-6 grid grid-cols-3 gap-3 sm:grid-cols-5 lg:grid-cols-9">
          {CATEGORIES.map((c) => (
            <Link key={c.label} to="/menu" search={{ q: c.label }} className="card-hover flex flex-col items-center gap-2 rounded-2xl border bg-card px-2 py-4 shadow-soft">
              <span className="sticker text-4xl">{c.emoji}</span>
              <span className="text-sm font-semibold">{c.label}</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-8">
        <div className="flex items-end justify-between">
          <h2 className="text-2xl font-bold">Featured restaurants</h2>
          <Link to="/restaurants" className="text-sm font-semibold text-primary">See all →</Link>
        </div>
        <div className="mt-6">
          {rq.isLoading ? <Loading /> : rq.error ? <ErrorState message={errorMessage(rq.error)} onRetry={() => rq.refetch()} /> : (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{featured.map((r) => <RestaurantCard key={r.id} r={r} />)}</div>
          )}
        </div>
      </section>

      {first && popular.data && popular.data.length > 0 && (
        <section className="mx-auto max-w-6xl px-4 py-8">
          <h2 className="text-2xl font-bold">Popular at {first.name}</h2>
          <div className="mt-6 grid gap-4 md:grid-cols-2">
            {popular.data.slice(0, 4).map((m) => <MenuItemCard key={m.id} item={m} restaurant={first} />)}
          </div>
        </section>
      )}
    </div>
  );
}
