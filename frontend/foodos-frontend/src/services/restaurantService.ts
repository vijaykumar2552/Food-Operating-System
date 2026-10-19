// Provides typed FoodOS restaurant service operations for the React application.
// Requests use the shared API client to preserve Java endpoint and response contracts.

import { api } from "./api";
import type { Restaurant } from "@/types";

function mapRestaurant(value: Restaurant): Restaurant {
  return { ...value, isOpen: value.status === "ACTIVE" };
}

export type RestaurantInput = Omit<Restaurant, "id">;

function restaurantPayload(body: Partial<RestaurantInput>) {
  const { isOpen, ...fields } = body;
  return {
    ...fields,
    ...(isOpen === undefined ? {} : { status: isOpen ? "ACTIVE" as const : "CLOSED" as const }),
  };
}

export const restaurantService = {
  list: async () => (await api.get<Restaurant[]>("/restaurants")).map(mapRestaurant),
  get: async (id: number) => mapRestaurant(await api.get<Restaurant>(`/restaurants/${id}`)),
  adminList: async () => (await api.get<Restaurant[]>("/admin/restaurants")).map(mapRestaurant),
  create: (body: RestaurantInput) => api.post<{ restaurantId: number }>("/admin/restaurants", restaurantPayload(body)),
  update: (id: number, body: Partial<RestaurantInput>) => api.put<{ message: string }>(`/admin/restaurants/${id}`, restaurantPayload(body)),
  remove: (id: number) => api.delete<void>(`/admin/restaurants/${id}`),
};
