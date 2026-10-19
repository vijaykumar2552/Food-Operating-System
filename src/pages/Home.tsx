// Renders the root frontend landing page and restaurant discovery experience.
// React state and API-backed data connect search and navigation to restaurant pages.

import { useEffect, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import type { Restaurant } from '../types'
import { api } from '../lib/api'
import { useCart } from '../hooks/useCart'

export default function Home() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { itemCount } = useCart()

  useEffect(() => {
    setLoading(true)
    api.restaurants(search).then(setRestaurants).catch(error => setError(error.message)).finally(() => setLoading(false))
  }, [search])

  return <>
    <header className="customer-header">
      <Link to="/" className="brand">Food<span>Flow</span></Link>
      <nav><NavLink to="/" end>Home</NavLink><NavLink to="/orders">Orders</NavLink><NavLink to="/settings">Account</NavLink><Link to="/cart" className="btn btn-primary btn-sm">Cart{itemCount ? ` (${itemCount})` : ''}</Link></nav>
    </header>
    <div style={{ padding: '32px 24px', maxWidth: 1200, margin: '0 auto' }}>
      <input placeholder="Search restaurants, cuisines, or locations..." value={search} onChange={event => setSearch(event.target.value)} style={{ maxWidth: 500, marginBottom: 24 }} />
      {error && <div className="form-error">{error}</div>}
      {loading ? <div className="loading-center"><div className="spinner" /></div> : <>
        <div className="section-title">Restaurants ({restaurants.length})</div>
        <div className="restaurant-grid">
          {restaurants.map(restaurant => <Link key={restaurant.id} to={`/restaurant/${restaurant.id}`} className="restaurant-card" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="cover"><span>{restaurant.name}</span><div className="status-badge"><span className={`badge ${restaurant.status === 'OPEN' ? 'badge-success' : 'badge-warning'}`}>{restaurant.status === 'OPEN' ? 'Open' : 'Unavailable'}</span></div></div>
            <div className="info"><h3>{restaurant.name}</h3><div className="cuisine">{restaurant.cuisine} · {restaurant.address}</div><div className="meta"><span>★ {restaurant.rating.toFixed(1)}</span><span>{restaurant.deliveryTimeMinutes} min</span><span>₹{restaurant.deliveryCharge} delivery</span></div></div>
          </Link>)}
        </div>
        {!restaurants.length && <div className="empty-state"><p>No restaurants found.</p></div>}
      </>}
      <div style={{ marginTop: 40, textAlign: 'center', color: 'var(--muted)', fontSize: 13 }}>FoodOS — MySQL-backed ordering</div>
    </div>
  </>
}
