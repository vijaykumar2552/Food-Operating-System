// Defines shared TypeScript models for the root frontend catalog and orders.
// Interfaces keep React state, API mapping, and IndexedDB records consistently typed.

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  cuisine: string;
  address: string;
  contactPhone: string;
  openingHours: string;
  rating: number;
  deliveryTimeMinutes: number;
  deliveryDistanceKm: number;
  deliveryCharge: number;
  status: 'OPEN' | 'CLOSED' | 'TEMPORARILY_UNAVAILABLE';
  serverStatus?: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
  coverImageId: string | null;
  galleryImageIds: string[];
  mapAddress: string;
  mapLat: number | null;
  mapLng: number | null;
  mapLink: string;
  createdAt: number;
  updatedAt: number;
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  categoryId: string;
  price: number;
  discountPrice: number | null;
  preparationTimeMinutes: number;
  isVeg: boolean;
  availability: 'AVAILABLE' | 'SOLD_OUT' | 'HIDDEN';
  imageId: string | null;
  createdAt: number;
  updatedAt: number;
}

export interface Category {
  id: string;
  name: string;
  restaurantId: string | null;
  sortOrder: number;
  createdAt: number;
}

export interface Offer {
  id: string;
  title: string;
  description: string;
  discountPercent: number;
  validUntil: number;
  imageId: string | null;
  createdAt: number;
}

export interface Order {
  id: string;
  customerName: string;
  restaurantId: string;
  restaurantName: string;
  items: OrderItem[];
  subtotal: number;
  discount: number;
  deliveryFee: number;
  total: number;
  status: 'PLACED' | 'ACCEPTED' | 'PREPARING' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
  paymentMethod: string;
  address: string;
  placedAt: number;
}

export interface OrderItem {
  itemId: string;
  name: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface StoredImage {
  id: string;
  blob: Blob;
  type: string;
  createdAt: number;
}
