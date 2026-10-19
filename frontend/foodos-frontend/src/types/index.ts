// Defines shared TypeScript request and response types for the FoodOS API.
// Field names and values follow the Java ApiHandler JSON contract.

export type Role = "CUSTOMER" | "ADMIN" | "RESTAURANT_OWNER" | "RESTAURANT_STAFF";

export interface User {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  role: Role;
}

export interface Restaurant {
  id: number;
  name: string;
  cuisine?: string;
  description?: string;
  location?: string;
  rating?: number;
  deliveryTimeMinutes?: number;
  deliveryCharge?: number;
  isOpen?: boolean;
  status?: "PENDING" | "ACTIVE" | "SUSPENDED" | "CLOSED";
}

export interface MenuItem {
  id: number;
  restaurantId: number;
  name: string;
  description?: string;
  price: number;
  category?: string;
  categoryId?: number;
  availability?: "AVAILABLE" | "SOLD_OUT" | "HIDDEN";
  available: boolean;
  stock?: number;
}

export interface OrderItem {
  menuItemId: number;
  name?: string;
  quantity: number;
  price?: number;
}

export interface Order {
  id: number;
  restaurantId?: number;
  restaurantName?: string;
  customerName?: string;
  orderDate?: string;
  items?: OrderItem[];
  totalAmount: number;
  paymentMethod?: string;
  paymentStatus?: string;
  status: string;
}

export interface PlaceOrderRequest {
  items: { itemId: number; quantity: number }[];
  addressLine: string;
  city: string;
  postalCode: string;
  paymentMethod: "CASH";
}

export interface RegisterRequest {
  fullName: string;
  phone: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export const ORDER_STATUSES = ["PLACED", "ACCEPTED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "REJECTED", "CANCELLED"];
