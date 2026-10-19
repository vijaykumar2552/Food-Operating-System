// Provides typed FoodOS menu service operations for the React application.
// Requests use the shared API client to preserve Java endpoint and response contracts.

import { api } from "./api";
import type { MenuItem } from "@/types";

export type MenuItemInput = Omit<MenuItem, "id">;

function mapMenuItem(item: MenuItem): MenuItem {
  return { ...item, available: item.availability === "AVAILABLE" };
}

export const menuService = {
  byRestaurant: (restaurantId: number) => api.get<MenuItem[]>(`/restaurants/${restaurantId}/menu`),
  adminList: async (restaurantId?: number) =>
    (await api.get<MenuItem[]>(`/admin/menu-items${restaurantId ? `?restaurantId=${restaurantId}` : ""}`)).map(mapMenuItem),
  categories: () => api.get<Array<{ id: number; restaurantId: number; name: string; sortOrder: number }>>("/admin/categories"),
  create: (body: { categoryId: number; name: string; description?: string; price: number; discountPrice?: number | null; preparationTimeMinutes?: number; isVeg?: boolean; availability?: "AVAILABLE" | "SOLD_OUT" | "HIDDEN" }) =>
    api.post<{ itemId: number }>("/admin/menu-items", body),
  update: (id: number, body: Partial<{ categoryId: number; name: string; description: string; price: number; discountPrice: number | null; preparationTimeMinutes: number; isVeg: boolean; availability: "AVAILABLE" | "SOLD_OUT" | "HIDDEN" }>) =>
    api.put<{ message: string }>(`/admin/menu-items/${id}`, body),
  remove: (id: number) => api.delete<void>(`/admin/menu-items/${id}`),
};
