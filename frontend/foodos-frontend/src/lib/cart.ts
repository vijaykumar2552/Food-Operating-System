// Pure cart math, kept separate so it can be unit tested.
import type { MenuItem } from "@/types";

export interface CartLine {
  item: MenuItem;
  quantity: number;
}

export const MAX_QTY = 20;

export function clampQty(q: number) {
  return Math.max(1, Math.min(MAX_QTY, Math.floor(q)));
}

export function subtotal(lines: CartLine[]) {
  return lines.reduce((sum, l) => sum + l.item.price * l.quantity, 0);
}

export function estimateTotal(lines: CartLine[], deliveryCharge = 0) {
  return lines.length ? subtotal(lines) + deliveryCharge : 0;
}
