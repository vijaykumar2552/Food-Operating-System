// Provides typed FoodOS order service operations for the React application.
// Requests use the shared API client to preserve Java endpoint and response contracts.

import { api } from "./api";
import type { Order, PlaceOrderRequest } from "@/types";

type CheckoutResponse = { orderId: number };
type StatusResponse = { message: string };

export const orderService = {
  my: () => api.get<Order[]>("/orders/my"),
  place: (body: PlaceOrderRequest) => api.post<CheckoutResponse>("/orders", body),
  adminList: () => api.get<Order[]>("/admin/orders"),
  updateStatus: (id: number, status: string) => api.put<StatusResponse>(`/admin/orders/${id}`, { status }),
};
