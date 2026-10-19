// PREVIEW ONLY. Sample data that mimics the Java API so the UI can be demonstrated.
// Nothing here is real database data. Disable via USE_MOCK in src/services/api.ts.
import type { MenuItem, Order, Restaurant, User } from "@/types";

let restaurants: Restaurant[] = [
  { id: 1, name: "Slice Republic", cuisine: "Italian", description: "Wood-fired pizzas and creamy pastas.", location: "MG Road", rating: 4.5, deliveryTimeMinutes: 30, deliveryCharge: 40, isOpen: true },
  { id: 2, name: "Biryani Junction", cuisine: "Hyderabadi", description: "Slow-cooked dum biryani and kebabs.", location: "Koramangala", rating: 4.7, deliveryTimeMinutes: 35, deliveryCharge: 30, isOpen: true },
  { id: 3, name: "Burger Barn", cuisine: "American", description: "Smash burgers, loaded fries and shakes.", location: "Indiranagar", rating: 4.3, deliveryTimeMinutes: 25, deliveryCharge: 35, isOpen: true },
  { id: 4, name: "Wok This Way", cuisine: "Chinese", description: "Hakka noodles, momos and fried rice.", location: "HSR Layout", rating: 4.2, deliveryTimeMinutes: 40, deliveryCharge: 25, isOpen: false },
  { id: 5, name: "Green Bowl", cuisine: "Healthy", description: "Fresh salads, wraps and smoothies.", location: "Whitefield", rating: 4.4, deliveryTimeMinutes: 20, deliveryCharge: 0, isOpen: true },
  { id: 6, name: "Sweet Tooth", cuisine: "Desserts", description: "Cakes, brownies and ice cream sundaes.", location: "Jayanagar", rating: 4.6, deliveryTimeMinutes: 30, deliveryCharge: 20, isOpen: true },
];

let nextMenuId = 100;
let menu: MenuItem[] = [
  { id: 1, restaurantId: 1, name: "Margherita Pizza", description: "Tomato, mozzarella, basil.", price: 249, category: "Pizza", available: true },
  { id: 2, restaurantId: 1, name: "Farmhouse Pizza", description: "Capsicum, onion, mushroom, corn.", price: 329, category: "Pizza", available: true },
  { id: 3, restaurantId: 1, name: "Alfredo Pasta", description: "Creamy white sauce penne.", price: 279, category: "Pasta", available: false },
  { id: 4, restaurantId: 1, name: "Garlic Bread", description: "Buttery, cheesy, golden.", price: 129, category: "Sides", available: true },
  { id: 5, restaurantId: 2, name: "Chicken Dum Biryani", description: "Basmati rice, tender chicken, saffron.", price: 299, category: "Biryani", available: true },
  { id: 6, restaurantId: 2, name: "Veg Biryani", description: "Seasonal vegetables, fragrant spices.", price: 229, category: "Biryani", available: true },
  { id: 7, restaurantId: 2, name: "Seekh Kebab", description: "Charcoal-grilled minced kebabs.", price: 249, category: "Starters", available: true },
  { id: 8, restaurantId: 3, name: "Classic Smash Burger", description: "Double patty, cheddar, pickles.", price: 219, category: "Burgers", available: true },
  { id: 9, restaurantId: 3, name: "Loaded Fries", description: "Cheese sauce, jalapeños.", price: 149, category: "Sides", available: true },
  { id: 10, restaurantId: 3, name: "Chocolate Shake", description: "Thick and creamy.", price: 159, category: "Drinks", available: true },
  { id: 11, restaurantId: 4, name: "Hakka Noodles", description: "Wok-tossed with veggies.", price: 189, category: "Noodles", available: true },
  { id: 12, restaurantId: 4, name: "Steamed Momos", description: "Six dumplings with chilli dip.", price: 149, category: "Starters", available: true },
  { id: 13, restaurantId: 5, name: "Greek Salad", description: "Feta, olives, cucumber.", price: 199, category: "Salads", available: true },
  { id: 14, restaurantId: 5, name: "Paneer Wrap", description: "Grilled paneer, mint mayo.", price: 179, category: "Sandwiches", available: true },
  { id: 15, restaurantId: 6, name: "Chocolate Truffle Cake", description: "Rich slice, ganache.", price: 169, category: "Desserts", available: true },
  { id: 16, restaurantId: 6, name: "Ice Cream Sundae", description: "Three scoops, hot fudge.", price: 189, category: "Desserts", available: true },
];

const users: (User & { password: string })[] = [
  // Preview-only demo accounts. Real roles always come from the Java backend.
  { id: 1, fullName: "Demo Customer", email: "customer@demo.foodos", phone: "9876543210", role: "CUSTOMER", password: "demo1234" },
  { id: 2, fullName: "Demo Admin", email: "admin@demo.foodos", phone: "9876500000", role: "ADMIN", password: "demo1234" },
];

let orders: Order[] = [
  { id: 1001, restaurantId: 2, restaurantName: "Biryani Junction", customerName: "Demo Customer", orderDate: "2026-10-10T13:20:00", items: [{ menuItemId: 5, name: "Chicken Dum Biryani", quantity: 2, price: 299 }], totalAmount: 628, paymentMethod: "COD", paymentStatus: "PENDING", status: "DELIVERED" },
  { id: 1002, restaurantId: 1, restaurantName: "Slice Republic", customerName: "Demo Customer", orderDate: "2026-10-12T20:05:00", items: [{ menuItemId: 1, name: "Margherita Pizza", quantity: 1, price: 249 }, { menuItemId: 4, name: "Garlic Bread", quantity: 1, price: 129 }], totalAmount: 418, paymentMethod: "COD", paymentStatus: "PENDING", status: "PREPARING" },
];
const ownerOf: Record<number, number> = { 1001: 1, 1002: 1 };

let currentUserId: number | null = null;

class MockError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

const delay = () => new Promise((r) => setTimeout(r, 120));
const strip = (u: User & { password: string }): User => ({ id: u.id, fullName: u.fullName, email: u.email, phone: u.phone, role: u.role });

function me() {
  if (typeof window !== "undefined" && currentUserId === null) {
    const saved = localStorage.getItem("foodos_mock_uid");
    if (saved) currentUserId = Number(saved);
  }
  const u = users.find((x) => x.id === currentUserId);
  if (!u) throw new MockError("Please log in to continue.", 401);
  return u;
}
function admin() {
  const u = me();
  if (u.role !== "ADMIN") throw new MockError("Admins only.", 403);
  return u;
}
function setSession(id: number | null) {
  currentUserId = id;
  if (typeof window === "undefined") return;
  if (id) localStorage.setItem("foodos_mock_uid", String(id));
  else localStorage.removeItem("foodos_mock_uid");
}

// Persist preview data in this browser so admin edits survive reloads and are
// seen by customer pages in other tabs (in-memory state reset on every reload).
const STORE_KEY = "foodos_mock_data_v1";
let hydrated = false;
if (typeof window !== "undefined") {
  // Pick up admin changes made in another tab without re-parsing on every call.
  window.addEventListener("storage", (e) => { if (e.key === STORE_KEY) hydrated = false; });
}
function hydrate() {
  if (hydrated || typeof window === "undefined") return;
  hydrated = true;
  try {
    const raw = localStorage.getItem(STORE_KEY);
    if (!raw) return;
    const d = JSON.parse(raw);
    restaurants = d.restaurants ?? restaurants;
    menu = d.menu ?? menu;
    orders = d.orders ?? orders;
    nextMenuId = d.nextMenuId ?? nextMenuId;
    Object.assign(ownerOf, d.ownerOf ?? {});
  } catch {
    /* ignore corrupt preview data */
  }
}
function persist() {
  if (typeof window === "undefined") return;
  localStorage.setItem(STORE_KEY, JSON.stringify({ restaurants, menu, orders, nextMenuId, ownerOf }));
}

export async function mockRequest<T>(method: string, path: string, body?: unknown): Promise<T> {
  await delay();
  hydrate();
  const result = await handle<T>(method, path, body);
  if (method !== "GET") persist();
  return result;
}

async function handle<T>(method: string, path: string, body?: unknown): Promise<T> {
  const b = (body ?? {}) as Record<string, unknown>;
  const seg = path.split("?")[0].split("/").filter(Boolean);
  const r = <R>(v: R) => v as unknown as T;

  if (path === "/health") return r({ status: "UP (mock)" });

  if (seg[0] === "auth") {
    if (seg[1] === "login") {
      const u = users.find((x) => x.email === String(b.email).toLowerCase() && x.password === b.password);
      if (!u) throw new MockError("Invalid email or password.", 401);
      setSession(u.id);
      return r({ ...strip(u), token: "mock-token" });
    }
    if (seg[1] === "register") {
      if (users.some((x) => x.email === String(b.email).toLowerCase())) throw new MockError("An account with this email already exists.", 409);
      const u = { id: users.length + 1, fullName: String(b.fullName), email: String(b.email).toLowerCase(), phone: String(b.phone), role: "CUSTOMER" as const, password: String(b.password) };
      users.push(u);
      return r({ message: "Registration successful." });
    }
    if (seg[1] === "logout") {
      setSession(null);
      return r(null);
    }
  }

  if (seg[0] === "restaurants") {
    if (seg.length === 1) return r(restaurants);
    const rest = restaurants.find((x) => x.id === Number(seg[1]));
    if (!rest) throw new MockError("Restaurant not found.", 404);
    if (seg[2] === "menu") return r(menu.filter((m) => m.restaurantId === rest.id));
    return r(rest);
  }

  if (seg[0] === "orders") {
    const u = me();
    if (method === "GET") return r(orders.filter((o) => ownerOf[o.id] === u.id).sort((a, b2) => b2.id - a.id));
    const items = (b.items as { menuItemId: number; quantity: number }[]) ?? [];
    const rest = restaurants.find((x) => x.id === Number(b.restaurantId));
    if (!rest) throw new MockError("Restaurant not found.", 400);
    if (!rest.isOpen) throw new MockError("This restaurant is currently closed.", 400);
    if (!items.length) throw new MockError("Your cart is empty.", 400);
    let subtotal = 0;
    const lines = items.map((it) => {
      const m = menu.find((x) => x.id === it.menuItemId);
      if (!m || !m.available) throw new MockError(`An item is no longer available.`, 400);
      if (it.quantity < 1 || it.quantity > 20) throw new MockError("Invalid quantity.", 400);
      subtotal += m.price * it.quantity;
      return { menuItemId: m.id, name: m.name, quantity: it.quantity, price: m.price };
    });
    const order: Order = { id: Math.max(...orders.map((o) => o.id), 1000) + 1, restaurantId: rest.id, restaurantName: rest.name, customerName: u.fullName, orderDate: new Date().toISOString(), items: lines, totalAmount: subtotal + (rest.deliveryCharge ?? 0), paymentMethod: "COD", paymentStatus: "PENDING", status: "PLACED" };
    orders = [order, ...orders];
    ownerOf[order.id] = u.id;
    return r(order);
  }

  if (seg[0] === "admin") {
    admin();
    const id = Number(seg[2]);
    if (seg[1] === "restaurants") {
      if (method === "GET") return r(restaurants);
      if (method === "POST") {
        const n = { ...(b as Omit<Restaurant, "id">), id: Math.max(0, ...restaurants.map((x) => x.id)) + 1 };
        restaurants = [...restaurants, n];
        return r(n);
      }
      if (method === "PUT") {
        restaurants = restaurants.map((x) => (x.id === id ? { ...x, ...b, id } : x));
        return r(restaurants.find((x) => x.id === id));
      }
      if (method === "DELETE") {
        restaurants = restaurants.filter((x) => x.id !== id);
        menu = menu.filter((m) => m.restaurantId !== id);
        return r(null);
      }
    }
    if (seg[1] === "menu-items") {
      if (method === "POST") {
        if (!restaurants.some((x) => x.id === Number(b.restaurantId))) throw new MockError("Restaurant not found.", 400);
        const n = { ...(b as Omit<MenuItem, "id">), restaurantId: Number(b.restaurantId), price: Number(b.price), id: nextMenuId++ };
        menu = [...menu, n];
        return r(n);
      }
      if (method === "PUT") {
        if (!menu.some((x) => x.id === id)) throw new MockError("Menu item not found.", 404);
        menu = menu.map((x) => (x.id === id ? { ...x, ...b, price: Number(b.price ?? x.price), id } : x));
        return r(menu.find((x) => x.id === id));
      }
      if (method === "DELETE") {
        menu = menu.filter((x) => x.id !== id);
        return r(null);
      }
    }
    if (seg[1] === "orders") {
      if (method === "GET") return r([...orders].sort((a, b2) => b2.id - a.id));
      if (method === "PUT") {
        orders = orders.map((o) => (o.id === id ? { ...o, status: String(b.status) } : o));
        return r(orders.find((o) => o.id === id));
      }
    }
  }

  throw new MockError(`Mock route not implemented: ${method} ${path}`, 404);
}
