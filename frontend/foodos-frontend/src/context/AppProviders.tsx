// Shares FoodOS app providers state with React routes through a context provider.
// Context keeps related state updates available across the component tree.

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { MenuItem, Restaurant, User } from "@/types";
import { clampQty, type CartLine } from "@/lib/cart";
import { authService } from "@/services/authService";

/* ---------- Auth ---------- */
interface AuthCtx {
  user: User | null;
  ready: boolean;
  setUser: (u: User | null) => void;
  logout: () => Promise<void>;
}
const AuthContext = createContext<AuthCtx | null>(null);
const USER_KEY = "foodos_user"; // display cache only; backend remains the authority

/* ---------- Cart ---------- */
interface CartCtx {
  lines: CartLine[];
  restaurant: Restaurant | null;
  add: (item: MenuItem, restaurant: Restaurant) => "added" | "other-restaurant" | "unavailable";
  setQty: (id: number, q: number) => void;
  remove: (id: number) => void;
  clear: () => void;
  count: number;
}
const CartContext = createContext<CartCtx | null>(null);
const CART_KEY = "foodos_cart";

export function AppProviders({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<User | null>(null);
  const [ready, setReady] = useState(false);
  const [lines, setLines] = useState<CartLine[]>([]);
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);

  useEffect(() => {
    try {
      const u = localStorage.getItem(USER_KEY);
      if (u) setUserState(JSON.parse(u));
      const c = localStorage.getItem(CART_KEY);
      if (c) {
        const parsed = JSON.parse(c);
        setLines(parsed.lines ?? []);
        setRestaurant(parsed.restaurant ?? null);
      }
    } catch {
      /* ignore corrupt storage */
    }
    setReady(true);
  }, []);

  useEffect(() => {
    if (ready) localStorage.setItem(CART_KEY, JSON.stringify({ lines, restaurant }));
  }, [lines, restaurant, ready]);

  const setUser = (u: User | null) => {
    setUserState(u);
    if (u) localStorage.setItem(USER_KEY, JSON.stringify(u));
    else localStorage.removeItem(USER_KEY);
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
    }
  };

  const cart: CartCtx = {
    lines,
    restaurant,
    count: lines.reduce((s, l) => s + l.quantity, 0),
    add(item, rest) {
      if (!item.available) return "unavailable";
      if (restaurant && restaurant.id !== rest.id && lines.length) return "other-restaurant";
      setRestaurant(rest);
      setLines((prev) => {
        const found = prev.find((l) => l.item.id === item.id);
        if (found) return prev.map((l) => (l.item.id === item.id ? { ...l, quantity: clampQty(l.quantity + 1) } : l));
        return [...prev, { item, quantity: 1 }];
      });
      return "added";
    },
    setQty: (id, q) => setLines((p) => p.map((l) => (l.item.id === id ? { ...l, quantity: clampQty(q) } : l))),
    remove: (id) =>
      setLines((p) => {
        const next = p.filter((l) => l.item.id !== id);
        if (!next.length) setRestaurant(null);
        return next;
      }),
    clear: () => {
      setLines([]);
      setRestaurant(null);
    },
  };

  return (
    <AuthContext.Provider value={{ user, ready, setUser, logout }}>
      <CartContext.Provider value={cart}>{children}</CartContext.Provider>
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const c = useContext(AuthContext);
  if (!c) throw new Error("useAuth must be inside AppProviders");
  return c;
}
export function useCart() {
  const c = useContext(CartContext);
  if (!c) throw new Error("useCart must be inside AppProviders");
  return c;
}
