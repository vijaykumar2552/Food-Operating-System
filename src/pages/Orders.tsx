// Shows customer order history in the root frontend.
// React effects load persisted orders and render their status and totals.

import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, type ApiOrder, currentUser } from '../lib/api'

export default function Orders() {
  const [orders, setOrders] = useState<ApiOrder[]>([])
  const [error, setError] = useState('')
  useEffect(() => { api.myOrders().then(setOrders).catch(error => setError(error.message)) }, [])
  return <>
    <header className="customer-header"><Link to="/" className="brand">Food<span>Flow</span></Link><nav><Link to="/" className="btn btn-ghost btn-sm">← Back</Link></nav></header>
    <div style={{ maxWidth: 700, margin: '0 auto', padding: 24 }}><h1 style={{ marginBottom: 24 }}>Your Orders</h1>
      {!currentUser() && <div className="empty-state"><p>Please log in to view your orders.</p><Link to="/settings" className="btn btn-primary">Open Account</Link></div>}
      {error && <div className="form-error">{error}</div>}
      {currentUser() && !orders.length && !error && <div className="empty-state"><p>No orders yet.</p></div>}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>{orders.map(order => <div key={order.id} className="card" style={{ padding: 20 }}><div className="flex-between"><strong>Order #{order.id}</strong><span className="badge badge-info">{order.status.replace(/_/g, ' ')}</span></div><div className="text-muted" style={{ marginTop: 8 }}>{order.restaurantName} · {new Date(order.orderDate).toLocaleString()}</div><div className="flex-between" style={{ marginTop: 12, fontWeight: 700 }}><span>Total</span><span>₹{order.totalAmount}</span></div></div>)}</div>
    </div>
  </>
}
