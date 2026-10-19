// Exposes cart operations and state to React components in the root frontend.
// The hook persists cart entries in browser storage and delegates API actions.

import { useCallback, useState } from 'react'
import type { MenuItem, Order, Restaurant } from '../types'
import { api } from '../lib/api'

export interface CartEntry { item: MenuItem; quantity: number }
const CART_KEY = 'foodos-cart'

function load(): CartEntry[] {
  try { return JSON.parse(localStorage.getItem(CART_KEY) || '[]') as CartEntry[] } catch { return [] }
}
function save(entries: CartEntry[]) { localStorage.setItem(CART_KEY, JSON.stringify(entries)) }

export function useCart() {
  const [entries, setEntries] = useState<CartEntry[]>(load)
  const update = (next: CartEntry[]) => { setEntries(next); save(next) }
  const addToCart = useCallback((item: MenuItem, qty = 1) => {
    setEntries(previous => {
      const existing = previous.find(entry => entry.item.id === item.id)
      const next = existing
        ? previous.map(entry => entry.item.id === item.id ? { ...entry, quantity: entry.quantity + qty } : entry)
        : [...previous, { item, quantity: qty }]
      save(next)
      return next
    })
  }, [])
  const setQuantity = useCallback((itemId: string, quantity: number) => {
    update(entries.map(entry => entry.item.id === itemId ? { ...entry, quantity } : entry).filter(entry => entry.quantity > 0))
  }, [entries])
  const removeFromCart = useCallback((itemId: string) => update(entries.filter(entry => entry.item.id !== itemId)), [entries])
  const clearCart = useCallback(() => update([]), [])
  const subtotal = entries.reduce((sum, entry) => sum + (entry.item.discountPrice ?? entry.item.price) * entry.quantity, 0)
  const deliveryFee = entries.length && subtotal < 500 ? 40 : 0
  const total = subtotal + deliveryFee
  const itemCount = entries.reduce((sum, entry) => sum + entry.quantity, 0)
  const restaurant: Restaurant | null = entries.length ? {
    id: entries[0].item.restaurantId, name: 'Selected restaurant', description: '', cuisine: '', address: '',
    contactPhone: '', openingHours: '', rating: 0, deliveryTimeMinutes: 0, deliveryDistanceKm: 0,
    deliveryCharge: deliveryFee, status: 'OPEN', coverImageId: null, galleryImageIds: [], mapAddress: '',
    mapLat: null, mapLng: null, mapLink: '', createdAt: 0, updatedAt: 0,
  } : null
  const placeOrder = useCallback(async (_name: string, address: string, _payment: string): Promise<Order> => {
    const parts = address.split(',').map(value => value.trim()).filter(Boolean)
    if (parts.length < 3) throw new Error('Enter address, city, and postal code separated by commas.')
    const result = await api.checkout({
      addressLine: parts[0],
      city: parts[1],
      postalCode: parts.slice(2).join(', '),
      items: entries.map(entry => ({ itemId: Number(entry.item.id), quantity: entry.quantity })),
    })
    clearCart()
    return {
      id: String(result.orderId), customerName: _name, restaurantId: entries[0]?.item.restaurantId || '',
      restaurantName: restaurant?.name || '', items: entries.map(entry => ({
        itemId: entry.item.id, name: entry.item.name, quantity: entry.quantity,
        unitPrice: entry.item.discountPrice ?? entry.item.price,
        lineTotal: (entry.item.discountPrice ?? entry.item.price) * entry.quantity,
      })), subtotal, discount: 0, deliveryFee, total, status: 'PLACED', paymentMethod: 'CASH',
      address, placedAt: Date.now(),
    }
  }, [entries, subtotal, deliveryFee, total, restaurant, clearCart])
  return { entries, subtotal, deliveryFee, total, itemCount, restaurant, addToCart, setQuantity, removeFromCart, clearCart, placeOrder }
}
