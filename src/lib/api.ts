// Defines typed requests and response mapping for the root frontend Java API client.
// Centralized endpoint calls keep backend payload handling separate from page components.

import type { MenuItem, Restaurant, Order } from '../types'

export interface ApiUser {
  id: number
  email: string
  fullName: string
  role: 'CUSTOMER' | 'ADMIN' | 'RESTAURANT_OWNER' | 'RESTAURANT_STAFF'
}

export interface ApiOrder {
  id: number
  restaurantName: string
  orderDate: string
  status: Order['status']
  totalAmount: number
}

export interface ApiMenuItem {
  id: number
  restaurantId: number
  category: string
  categoryId?: number
  name: string
  description: string
  price: number
  discountPrice: number | null
  preparationTimeMinutes: number
  isVeg: boolean
  availability: MenuItem['availability']
  available?: boolean
  stock: number
}

export interface ApiAdminOrder extends ApiOrder {
  customerId: number
}

interface ApiRestaurant {
  id: number
  name: string
  description: string | null
  cuisine: string
  location: string
  contactPhone?: string | null
  openingHours?: string | null
  rating: number
  deliveryTimeMinutes: number
  deliveryDistanceKm?: number
  deliveryCharge: number
  status: string
}

const TOKEN_KEY = 'foodos-api-token'
const USER_KEY = 'foodos-api-user'

export function currentToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function currentUser(): ApiUser | null {
  const raw = sessionStorage.getItem(USER_KEY)
  if (!raw) return null
  try { return JSON.parse(raw) as ApiUser } catch { return null }
}

export function setSession(user: ApiUser, token: string) {
  sessionStorage.setItem(TOKEN_KEY, token)
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearSession() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  const token = currentToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(`/api${path}`, { ...init, headers })
  if (response.status === 401) clearSession()
  const text = await response.text()
  let payload: { success?: boolean; data?: T | { error?: string }; error?: string } = {}
  try { payload = text ? JSON.parse(text) : {} } catch { throw new Error(`API returned invalid JSON (${response.status}).`) }
  const nestedError = payload.data && typeof payload.data === 'object' && 'error' in payload.data ? payload.data.error : undefined
  if (!response.ok) throw new Error(payload.error || nestedError || `API request failed (${response.status}).`)
  return payload.data as T
}

export const api = {
  health: () => request<{ status: string }>('/health'),
  restaurants: async (search = '') => (await request<ApiRestaurant[]>(`/restaurants${search ? `?search=${encodeURIComponent(search)}` : ''}`)).map(mapRestaurant),
  restaurant: async (id: string) => mapRestaurant(await request<ApiRestaurant>(`/restaurants/${id}`)),
  menu: (restaurantId: string, search = '', category = '') => {
    const params = new URLSearchParams()
    if (search) params.set('search', search)
    if (category) params.set('category', category)
    return request<ApiMenuItem[]>(`/restaurants/${restaurantId}/menu${params.toString() ? `?${params}` : ''}`)
  },
  register: (body: { fullName: string; email: string; password: string; phone: string }) =>
    request<{ message: string }>('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login: async (email: string, password: string) => {
    const data = await request<ApiUser & { token: string }>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
    setSession(data, data.token)
    return data
  },
  logout: async () => {
    try { await request<void>('/auth/logout', { method: 'POST' }) } finally { clearSession() }
  },
  cartAdd: (itemId: string, quantity: number) =>
    request<void>('/cart/items', { method: 'POST', body: JSON.stringify({ itemId: Number(itemId), quantity }) }),
  cartSetQuantity: (itemId: string, quantity: number) =>
    request<void>(`/cart/items/${itemId}`, { method: 'PUT', body: JSON.stringify({ quantity }) }),
  cartClear: () => request<void>('/cart', { method: 'DELETE' }),
  checkout: (body: { addressLine: string; city: string; postalCode: string; items: Array<{ itemId: number; quantity: number }> }) =>
    request<{ orderId: number }>('/orders', { method: 'POST', body: JSON.stringify({ ...body, paymentMethod: 'CASH' }) }),
  myOrders: () => request<ApiOrder[]>('/orders/my'),
  adminRestaurants: async () => (await request<ApiRestaurant[]>('/admin/restaurants')).map(mapRestaurant),
  createRestaurant: (body: Record<string, unknown>) => request<{ restaurantId: number }>('/admin/restaurants', { method: 'POST', body: JSON.stringify(body) }),
  updateRestaurant: (id: string, body: Record<string, unknown>) => request<void>(`/admin/restaurants/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteRestaurant: (id: string) => request<void>(`/admin/restaurants/${id}`, { method: 'DELETE' }),
  adminMenu: (restaurantId = '') => request<ApiMenuItem[]>(`/admin/menu-items${restaurantId ? `?restaurantId=${encodeURIComponent(restaurantId)}` : ''}`),
  adminCategories: () => request<Array<{ id: number; restaurantId: number; name: string; sortOrder: number }>>('/admin/categories'),
  createMenuItem: (body: Record<string, unknown>) => request<{ itemId: number }>('/admin/menu-items', { method: 'POST', body: JSON.stringify(body) }),
  updateMenuItem: (id: string, body: Record<string, unknown>) => request<void>(`/admin/menu-items/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteMenuItem: (id: string) => request<void>(`/admin/menu-items/${id}`, { method: 'DELETE' }),
  adminOrders: () => request<ApiAdminOrder[]>('/admin/orders'),
  updateOrderStatus: (id: string, status: string) => request<void>(`/admin/orders/${id}`, { method: 'PUT', body: JSON.stringify({ status }) }),
}

export function mapRestaurant(raw: ApiRestaurant): Restaurant {
  return {
    id: String(raw.id), name: raw.name, description: raw.description || '', cuisine: raw.cuisine,
    address: raw.location || '', contactPhone: raw.contactPhone || '', openingHours: raw.openingHours || '',
    rating: Number(raw.rating || 0), deliveryTimeMinutes: raw.deliveryTimeMinutes || 30,
    deliveryDistanceKm: Number(raw.deliveryDistanceKm || 0), deliveryCharge: Number(raw.deliveryCharge || 0),
    status: raw.status === 'ACTIVE' ? 'OPEN' : raw.status === 'PENDING' || raw.status === 'SUSPENDED' ? 'TEMPORARILY_UNAVAILABLE' : 'CLOSED',
    serverStatus: raw.status as Restaurant['serverStatus'],
    coverImageId: null, galleryImageIds: [], mapAddress: raw.location || '', mapLat: null, mapLng: null, mapLink: '',
    createdAt: Date.now(), updatedAt: Date.now(),
  }
}

export function mapMenuItem(value: ApiMenuItem): MenuItem {
  return {
    id: String(value.id), restaurantId: String(value.restaurantId), categoryId: String(value.categoryId ?? value.category),
    name: value.name, description: value.description || '', price: Number(value.price), discountPrice: value.discountPrice == null ? null : Number(value.discountPrice),
    preparationTimeMinutes: value.preparationTimeMinutes || 15, isVeg: value.isVeg, availability: value.availability,
    imageId: null, createdAt: Date.now(), updatedAt: Date.now(),
  }
}
