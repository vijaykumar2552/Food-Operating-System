// Displays a restaurant and its menu in the root frontend.
// React effects load API data while cart actions update shared cart state.

import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { MenuItem, Restaurant } from '../types'
import { api, mapMenuItem } from '../lib/api'
import { useCart } from '../hooks/useCart'

export default function RestaurantDetail() {
  const { id = '' } = useParams<{ id: string }>()
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null)
  const [items, setItems] = useState<MenuItem[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { addToCart, itemCount } = useCart()

  useEffect(() => {
    let active = true
    const load = async () => {
      try {
        const [restaurantValue, menu] = await Promise.all([api.restaurant(id), api.menu(id)])
        if (!active) return
        setRestaurant(restaurantValue)
        setItems(menu.map(mapMenuItem))
        setError('')
      } catch (caught) {
        if (active) setError(caught instanceof Error ? caught.message : 'Menu could not be loaded.')
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    window.addEventListener('focus', load)
    return () => {
      active = false
      window.removeEventListener('focus', load)
    }
  }, [id])

  if (loading) return <div className="loading-center"><div className="spinner" /></div>
  if (error || !restaurant) return <div className="empty-state"><p>{error || 'Restaurant not found.'}</p><Link to="/">Back to home</Link></div>
  const filtered = items.filter(item => item.availability !== 'HIDDEN' && item.name.toLowerCase().includes(search.toLowerCase()))
  return <>
    <header className="customer-header"><Link to="/" className="brand">Food<span>Flow</span></Link><nav><Link to="/" className="btn btn-ghost btn-sm">← Back</Link><Link to="/cart" className="btn btn-primary btn-sm">Cart ({itemCount})</Link></nav></header>
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <div className="card" style={{ padding: 24, marginBottom: 24 }}><h1>{restaurant.name}</h1><p>{restaurant.description}</p><div className="meta"><span>★ {restaurant.rating.toFixed(1)}</span><span>{restaurant.deliveryTimeMinutes} min</span><span>{restaurant.address}</span></div></div>
      <input placeholder="Search menu..." value={search} onChange={event => setSearch(event.target.value)} style={{ marginBottom: 24 }} />
      <div className="menu-grid">{filtered.map(item => <div key={item.id} className="menu-item-card" style={{ opacity: item.availability === 'SOLD_OUT' ? 0.6 : 1 }}><div className="item-img"><span>No photo</span></div><div className="item-info"><h4>{item.name}</h4><div className="desc">{item.description}</div><div className="item-price">₹{item.discountPrice ?? item.price}</div>{item.availability === 'SOLD_OUT' ? <span className="badge badge-muted">Sold Out</span> : <button className="btn btn-primary btn-sm" onClick={() => addToCart(item)}>Add +</button>}</div></div>)}</div>
      {!filtered.length && <div className="empty-state"><p>No menu items available.</p></div>}
    </div>
  </>
}
