// Renders the FoodOS checkout page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Banknote } from "lucide-react";
import { toast } from "sonner";
import { useAuth, useCart } from "@/context/AppProviders";
import { orderService } from "@/services/orderService";
import { errorMessage } from "@/services/api";
import { formatPrice } from "@/lib/food";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { EmptyState, PageHeader } from "@/components/common/States";
import { OrderSummary } from "./cart";

export const Route = createFileRoute("/checkout")({
  head: () => pageMeta("Checkout", "Place your FoodOS order with Cash on Delivery."),
  component: Checkout,
});

function Checkout() {
  const { user } = useAuth();
  const cart = useCart();
  const nav = useNavigate();
  const qc = useQueryClient();
  const [addressLine, setAddressLine] = useState("");
  const [city, setCity] = useState("");
  const [postalCode, setPostalCode] = useState("");
  const addressComplete = addressLine.trim().length > 0
    && city.trim().length > 0
    && postalCode.trim().length > 0;

  const place = useMutation({
    mutationFn: () =>
      orderService.place({
        items: cart.lines.map((l) => ({ itemId: l.item.id, quantity: l.quantity })),
        addressLine: addressLine.trim(),
        city: city.trim(),
        postalCode: postalCode.trim(),
        paymentMethod: "CASH",
      }),
    onSuccess: (result) => {
      cart.clear();
      qc.invalidateQueries({ queryKey: ["orders"] });
      toast.success(`Order #${result.orderId} placed.`);
      nav({ to: "/orders" });
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  if (!cart.lines.length) return <EmptyState emoji="🛒" title="Nothing to check out" action={<Button asChild className="rounded-full"><Link to="/restaurants">Browse restaurants</Link></Button>} />;
  if (!user) return <EmptyState emoji="🔐" title="Log in to place your order" action={<Button asChild className="rounded-full"><Link to="/login">Login / Register</Link></Button>} />;
  if (user.role !== "CUSTOMER") return <EmptyState emoji="🚫" title="Only customers can place orders" />;

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <PageHeader title="Checkout" subtitle={`Ordering from ${cart.restaurant?.name}`} />
      <div className="space-y-5 rounded-3xl border bg-card p-6 shadow-soft">
        <ul className="space-y-1 text-sm">
          {cart.lines.map((l) => (
            <li key={l.item.id} className="flex justify-between"><span>{l.quantity} × {l.item.name}</span><span>{formatPrice(l.item.price * l.quantity)}</span></li>
          ))}
        </ul>
        <div className="border-t pt-4"><OrderSummary /></div>
        <div className="space-y-3">
          <label className="block text-sm font-medium">Address
            <input value={addressLine} onChange={(e) => setAddressLine(e.target.value)} className="mt-1 w-full rounded-xl border bg-background px-3 py-2" required />
          </label>
          <div className="grid grid-cols-2 gap-3">
            <label className="block text-sm font-medium">City
              <input value={city} onChange={(e) => setCity(e.target.value)} className="mt-1 w-full rounded-xl border bg-background px-3 py-2" required />
            </label>
            <label className="block text-sm font-medium">Postal code
              <input value={postalCode} onChange={(e) => setPostalCode(e.target.value)} className="mt-1 w-full rounded-xl border bg-background px-3 py-2" required />
            </label>
          </div>
        </div>
        <div className="flex items-center gap-3 rounded-2xl border-2 border-primary bg-accent/50 p-4">
          <Banknote className="h-6 w-6 text-primary" />
          <div>
            <p className="font-semibold">Cash on Delivery</p>
            <p className="text-xs text-muted-foreground">Pay in cash when your food arrives. The only payment method available.</p>
          </div>
        </div>
        <Button
          type="button"
          size="lg"
          className="w-full rounded-full"
          disabled={place.isPending || !addressComplete}
          onClick={() => place.mutate()}
          aria-busy={place.isPending}
        >
          {place.isPending ? "Placing order…" : "Place order (COD)"}
        </Button>
      </div>
    </div>
  );
}
