// Renders the FoodOS cart page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link } from "@tanstack/react-router";
import { Minus, Plus, Trash2 } from "lucide-react";
import { useCart } from "@/context/AppProviders";
import { MAX_QTY, estimateTotal, subtotal } from "@/lib/cart";
import { foodEmoji, formatPrice } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { Sticker } from "@/components/food/Sticker";
import { EmptyState, PageHeader } from "@/components/common/States";

export const Route = createFileRoute("/cart")({
  head: () => pageMeta("Your cart", "Review your FoodOS cart before checkout."),
  component: CartPage,
});

export function OrderSummary() {
  const { lines, restaurant } = useCart();
  const delivery = restaurant?.deliveryCharge ?? 0;
  return (
    <div className="space-y-2 text-sm">
      <div className="flex justify-between"><span className="text-muted-foreground">Subtotal</span><span>{formatPrice(subtotal(lines))}</span></div>
      <div className="flex justify-between"><span className="text-muted-foreground">Delivery charge</span><span>{delivery === 0 ? "Free" : formatPrice(delivery)}</span></div>
      <div className="flex justify-between border-t pt-3 text-base font-bold"><span>Estimated total</span><span>{formatPrice(estimateTotal(lines, delivery))}</span></div>
      <p className="text-xs text-muted-foreground">Final amount is confirmed by the server using current prices.</p>
    </div>
  );
}

function CartPage() {
  const { lines, restaurant, setQty, remove, clear } = useCart();

  if (!lines.length)
    return <EmptyState emoji="🛒" title="Your cart is empty" text="Hungry? Find something delicious." action={<Button asChild className="rounded-full"><Link to="/restaurants">Browse restaurants</Link></Button>} />;

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <PageHeader title="Your cart" subtitle={restaurant ? `From ${restaurant.name}` : undefined}>
        <Button variant="ghost" onClick={clear}>Clear cart</Button>
      </PageHeader>
      <div className="grid gap-8 lg:grid-cols-[1fr_360px]">
        <div className="space-y-3">
          {lines.map(({ item, quantity }) => (
            <div key={item.id} className="flex items-center gap-4 rounded-3xl border bg-card p-4 shadow-soft">
              <Sticker emoji={foodEmoji(item.name, item.category)} seed={item.name} size="sm" />
              <div className="min-w-0 flex-1">
                <p className="font-semibold">{item.name}</p>
                <p className="text-sm text-muted-foreground">{formatPrice(item.price)} each</p>
              </div>
              <div className="flex items-center gap-1 rounded-full border bg-background p-1">
                <button aria-label="Decrease" disabled={quantity <= 1} onClick={() => setQty(item.id, quantity - 1)} className="grid h-8 w-8 place-items-center rounded-full hover:bg-secondary disabled:opacity-40"><Minus className="h-4 w-4" /></button>
                <span className="w-6 text-center font-semibold">{quantity}</span>
                <button aria-label="Increase" disabled={quantity >= MAX_QTY} onClick={() => setQty(item.id, quantity + 1)} className="grid h-8 w-8 place-items-center rounded-full hover:bg-secondary disabled:opacity-40"><Plus className="h-4 w-4" /></button>
              </div>
              <span className="hidden w-20 text-right font-bold sm:block">{formatPrice(item.price * quantity)}</span>
              <button aria-label="Remove" onClick={() => remove(item.id)} className="text-muted-foreground hover:text-destructive"><Trash2 className="h-5 w-5" /></button>
            </div>
          ))}
        </div>
        <aside className="h-fit rounded-3xl border bg-card p-6 shadow-soft">
          <h2 className="mb-4 text-lg font-bold">Bill details</h2>
          <OrderSummary />
          <Button asChild size="lg" className="mt-6 w-full rounded-full"><Link to="/checkout">Checkout</Link></Button>
        </aside>
      </div>
    </div>
  );
}
