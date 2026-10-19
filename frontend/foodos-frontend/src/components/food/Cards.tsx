// Implements the reusable FoodOS cards interface component.
// React props and event handlers connect this UI element to its parent workflow.

import { Link } from "@tanstack/react-router";
import { Bike, Clock, MapPin, Plus, Star } from "lucide-react";
import { toast } from "sonner";
import type { MenuItem, Restaurant } from "@/types";
import { foodEmoji, formatPrice } from "@/lib/food";
import { useCart } from "@/context/AppProviders";
import { Button } from "@/components/ui/button";
import { Sticker } from "./Sticker";

export function RestaurantCard({ r }: { r: Restaurant }) {
  return (
    <Link
      to="/restaurants/$id"
      params={{ id: String(r.id) }}
      className="card-hover group flex flex-col rounded-3xl border bg-card p-5 shadow-soft"
    >
      <div className="flex items-start justify-between">
        <Sticker emoji={foodEmoji(r.cuisine, r.name, r.description)} seed={r.id} />
        {r.isOpen !== undefined && (
          <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${r.isOpen ? "bg-success/15 text-success" : "bg-muted text-muted-foreground"}`}>
            {r.isOpen ? "Open" : "Closed"}
          </span>
        )}
      </div>
      <h3 className="mt-4 text-lg font-bold group-hover:text-primary">{r.name}</h3>
      {r.cuisine && <p className="text-sm font-medium text-accent-foreground">{r.cuisine}</p>}
      {r.description && <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">{r.description}</p>}
      <div className="mt-auto flex flex-wrap gap-x-4 gap-y-1 pt-4 text-xs text-muted-foreground">
        {r.rating !== undefined && (
          <span className="flex items-center gap-1 font-semibold text-foreground">
            <Star className="h-3.5 w-3.5 fill-warning text-warning" /> {r.rating.toFixed(1)}
          </span>
        )}
        {r.deliveryTimeMinutes !== undefined && (
          <span className="flex items-center gap-1"><Clock className="h-3.5 w-3.5" /> {r.deliveryTimeMinutes} min</span>
        )}
        {r.deliveryCharge !== undefined && (
          <span className="flex items-center gap-1"><Bike className="h-3.5 w-3.5" /> {r.deliveryCharge === 0 ? "Free delivery" : formatPrice(r.deliveryCharge)}</span>
        )}
        {r.location && <span className="flex items-center gap-1"><MapPin className="h-3.5 w-3.5" /> {r.location}</span>}
      </div>
    </Link>
  );
}

export function MenuItemCard({ item, restaurant }: { item: MenuItem; restaurant?: Restaurant }) {
  const cart = useCart();
  const closed = restaurant?.isOpen === false;
  const disabled = !item.available || closed || !restaurant;

  const onAdd = () => {
    if (!restaurant) return;
    const res = cart.add(item, restaurant);
    if (res === "added") toast.success(`${item.name} added to cart`);
    else if (res === "other-restaurant") toast.error(`Your cart has items from ${cart.restaurant?.name}. Clear it to order from here.`);
    else toast.error("This item is unavailable.");
  };

  return (
    <div className={`flex gap-4 rounded-3xl border bg-card p-4 shadow-soft ${!item.available ? "opacity-60" : ""}`}>
      <Sticker emoji={foodEmoji(item.name, item.category)} seed={item.name} size="sm" className="mt-1" />
      <div className="flex min-w-0 flex-1 flex-col">
        <div className="flex items-start justify-between gap-2">
          <h4 className="font-semibold leading-tight">{item.name}</h4>
          <span className="whitespace-nowrap font-bold">{formatPrice(item.price)}</span>
        </div>
        {item.category && <span className="text-xs font-medium text-accent-foreground">{item.category}{restaurant && ` · ${restaurant.name}`}</span>}
        {item.description && <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">{item.description}</p>}
        <div className="mt-3 flex items-center justify-between">
          <span className="text-xs text-muted-foreground">{!item.available ? "Unavailable" : closed ? "Restaurant closed" : ""}</span>
          <Button size="sm" onClick={onAdd} disabled={disabled} className="rounded-full">
            <Plus className="h-4 w-4" /> Add
          </Button>
        </div>
      </div>
    </div>
  );
}
