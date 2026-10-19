// Implements the reusable FoodOS site header interface component.
// React props and event handlers connect this UI element to its parent workflow.

import { Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { LogOut, Menu as MenuIcon, ShoppingBag, X } from "lucide-react";
import { useAuth, useCart } from "@/context/AppProviders";
import { USE_MOCK } from "@/services/api";
import { Button } from "@/components/ui/button";

const CUSTOMER_NAV = [
  { to: "/", label: "Home" },
  { to: "/restaurants", label: "Restaurants" },
  { to: "/menu", label: "Menu" },
  { to: "/orders", label: "My Orders" },
  { to: "/account", label: "Account" },
] as const;

const ADMIN_NAV = [
  { to: "/admin", label: "Dashboard" },
  { to: "/admin/restaurants", label: "Restaurants" },
  { to: "/admin/menu", label: "Menu" },
  { to: "/admin/orders", label: "Orders" },
] as const;

export function Logo() {
  return (
    <Link to="/" className="flex items-center gap-2">
      <span className="grid h-9 w-9 place-items-center rounded-xl bg-primary text-xl shadow-soft">🍔</span>
      <span className="font-display text-xl font-extrabold tracking-tight">
        Food<span className="text-primary">OS</span>
      </span>
    </Link>
  );
}

export function SiteHeader() {
  const { user, logout } = useAuth();
  const { count } = useCart();
  const nav = useNavigate();
  const [open, setOpen] = useState(false);
  const isAdmin = user?.role === "ADMIN";
  const links = isAdmin ? ADMIN_NAV : CUSTOMER_NAV;

  const doLogout = async () => {
    await logout();
    setOpen(false);
    nav({ to: "/" });
  };

  return (
    <>
      {USE_MOCK && (
        <div className="bg-foreground px-4 py-1.5 text-center text-xs font-medium text-background">
          Preview mode — showing sample data, not your real database.
        </div>
      )}
      <header className="sticky top-0 z-40 border-b bg-background/85 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-6xl items-center gap-6 px-4">
          <Logo />
          <nav className="hidden flex-1 items-center gap-1 md:flex">
            {links.map((l) => (
              <Link
                key={l.to}
                to={l.to}
                activeOptions={{ exact: l.to === "/" || l.to === "/admin" }}
                className="rounded-full px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
                activeProps={{ className: "bg-secondary text-foreground" }}
              >
                {l.label}
              </Link>
            ))}
          </nav>
          <div className="ml-auto flex items-center gap-2">
            {!isAdmin && (
              <Link to="/cart" className="relative grid h-10 w-10 place-items-center rounded-full border bg-card transition-colors hover:bg-secondary" aria-label="Cart">
                <ShoppingBag className="h-5 w-5" />
                {count > 0 && (
                  <span className="absolute -right-1 -top-1 grid h-5 min-w-5 place-items-center rounded-full bg-primary px-1 text-[11px] font-bold text-primary-foreground">{count}</span>
                )}
              </Link>
            )}
            {user ? (
              <Button variant="ghost" size="sm" onClick={doLogout} className="hidden md:inline-flex">
                <LogOut className="h-4 w-4" /> Logout
              </Button>
            ) : (
              <Button asChild size="sm" className="hidden rounded-full md:inline-flex">
                <Link to="/login">Login / Register</Link>
              </Button>
            )}
            <button className="grid h-10 w-10 place-items-center rounded-full border md:hidden" onClick={() => setOpen(!open)} aria-label="Menu">
              {open ? <X className="h-5 w-5" /> : <MenuIcon className="h-5 w-5" />}
            </button>
          </div>
        </div>
        {open && (
          <nav className="flex flex-col gap-1 border-t bg-background px-4 py-3 md:hidden">
            {links.map((l) => (
              <Link key={l.to} to={l.to} onClick={() => setOpen(false)} className="rounded-lg px-3 py-2 font-medium hover:bg-secondary">
                {l.label}
              </Link>
            ))}
            {!isAdmin && (
              <Link to="/cart" onClick={() => setOpen(false)} className="rounded-lg px-3 py-2 font-medium hover:bg-secondary">Cart ({count})</Link>
            )}
            {user ? (
              <button onClick={doLogout} className="rounded-lg px-3 py-2 text-left font-medium hover:bg-secondary">Logout</button>
            ) : (
              <Link to="/login" onClick={() => setOpen(false)} className="rounded-lg px-3 py-2 font-medium text-primary hover:bg-secondary">Login / Register</Link>
            )}
          </nav>
        )}
      </header>
    </>
  );
}

export function SiteFooter() {
  return (
    <footer className="mt-20 border-t">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-3 px-4 py-8 text-sm text-muted-foreground md:flex-row">
        <Logo />
        <p>Good food. Smarter choices. · Cash on Delivery only</p>
        <p className="text-2xl tracking-widest">🍕🍜🍰🥤</p>
      </div>
    </footer>
  );
}
