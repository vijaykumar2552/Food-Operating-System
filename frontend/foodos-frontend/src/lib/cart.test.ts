// Provides reusable FoodOS frontend helpers for cart test behavior.
// Keeps shared presentation or data logic outside route components.

import { describe, expect, it } from "vitest";
import { clampQty, estimateTotal, subtotal } from "./cart";

const item = (price: number) => ({ id: 1, restaurantId: 1, name: "x", price, available: true });

describe("cart math", () => {
  it("multiplies price by quantity", () => {
    expect(subtotal([{ item: item(100), quantity: 3 }, { item: item(50), quantity: 2 }])).toBe(400);
  });
  it("adds delivery charge to the estimate", () => {
    expect(estimateTotal([{ item: item(100), quantity: 1 }], 40)).toBe(140);
  });
  it("empty cart totals zero even with delivery charge", () => {
    expect(estimateTotal([], 40)).toBe(0);
  });
  it("clamps quantity between 1 and 20", () => {
    expect(clampQty(0)).toBe(1);
    expect(clampQty(25)).toBe(20);
  });
});
