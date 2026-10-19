// Renders the customer cart and checkout handoff in the root frontend.
// React state connects cart entries, quantity actions, and order submission.

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../hooks/useCart'
import type { Order } from '../types'
import { currentUser } from '../lib/api'

export default function Cart() {
  const navigate = useNavigate()
  const { entries, subtotal, deliveryFee, total, restaurant, setQuantity, removeFromCart, clearCart, placeOrder } = useCart()
  const [showCheckout, setShowCheckout] = useState(false)
  const [name, setName] = useState('')
  const [address, setAddress] = useState('')
  const [payment, setPayment] = useState('CASH')
  const [error, setError] = useState('')
  const [confirmOrder, setConfirmOrder] = useState<Order | null>(null)

  const handleCheckout = async () => {
    if (!currentUser()) { setError('Please log in before checkout.'); return }
    if (!name.trim()) { setError('Enter your name.'); return }
    if (address.trim().split(',').filter(Boolean).length < 3) { setError('Enter address, city, and postal code separated by commas.'); return }
    setError('')
    try {
      const order = await placeOrder(name, address, payment)
      setConfirmOrder(order)
      setShowCheckout(false)
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'Checkout failed.') }
  }

  if (confirmOrder) {
    return (
      <>
        <header className="customer-header">
          <Link to="/" className="brand">Food<span>Flow</span></Link>
        </header>
        <div style={{ maxWidth: 500, margin: '60px auto', padding: 24 }}>
          <div className="card text-center" style={{ padding: 40 }}>
            <div style={{ fontSize: 48, marginBottom: 16 }}>✓</div>
            <h2>Order Confirmed!</h2>
            <p style={{ color: 'var(--muted)', marginBottom: 20 }}>
              Your order <strong>#{confirmOrder.id.slice(-6)}</strong> from {confirmOrder.restaurantName} has been placed.
            </p>
            <div style={{ textAlign: 'left', marginBottom: 24 }}>
              {confirmOrder.items.map(i => (
                <div key={i.itemId} className="flex-between" style={{ padding: '6px 0', borderBottom: '1px solid var(--border-soft)' }}>
                  <span>{i.quantity}x {i.name}</span>
                  <span>₹{i.lineTotal}</span>
                </div>
              ))}
              <div className="flex-between" style={{ padding: '12px 0', fontWeight: 700 }}>
                <span>Total</span>
                <span>₹{confirmOrder.total}</span>
              </div>
            </div>
            <div className="flex gap-md" style={{ justifyContent: 'center' }}>
              <button className="btn btn-primary" onClick={() => navigate('/orders')}>View Orders</button>
              <button className="btn btn-secondary" onClick={() => navigate('/')}>Continue Shopping</button>
            </div>
          </div>
        </div>
      </>
    )
  }

  return (
    <>
      <header className="customer-header">
        <Link to="/" className="brand">Food<span>Flow</span></Link>
        <nav>
          <Link to="/" className="btn btn-ghost btn-sm">← Back</Link>
        </nav>
      </header>

      <div style={{ maxWidth: 700, margin: '0 auto', padding: 24 }}>
        <h1 style={{ marginBottom: 24 }}>Your Cart</h1>

        {entries.length === 0 ? (
          <div className="empty-state">
            <div className="icon">🛒</div>
            <p>Your cart is empty.</p>
            <Link to="/" className="btn btn-primary mt-md">Browse Restaurants</Link>
          </div>
        ) : (
          <>
            {restaurant && (
              <div className="text-muted mb-md" style={{ fontSize: 14 }}>
                From <strong>{restaurant.name}</strong>
              </div>
            )}

            <div className="card" style={{ padding: 0 }}>
              {entries.map((e, i) => (
                <div key={e.item.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px 20px', borderBottom: i < entries.length - 1 ? '1px solid var(--border-soft)' : 'none' }}>
                  <div style={{ flex: 1 }}>
                    <div className="fw-600">{e.item.name}</div>
                    <div className="text-muted" style={{ fontSize: 13 }}>
                      ₹{e.item.discountPrice ?? e.item.price} each
                    </div>
                  </div>
                  <div className="flex gap-sm" style={{ alignItems: 'center' }}>
                    <button className="btn btn-secondary btn-sm" onClick={() => setQuantity(e.item.id, e.quantity - 1)}>−</button>
                    <span style={{ minWidth: 30, textAlign: 'center', fontWeight: 600 }}>{e.quantity}</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => setQuantity(e.item.id, e.quantity + 1)}>+</button>
                    <span style={{ minWidth: 70, textAlign: 'right', fontWeight: 700 }}>
                      ₹{(e.item.discountPrice ?? e.item.price) * e.quantity}
                    </span>
                    <button className="btn btn-ghost btn-sm" onClick={() => removeFromCart(e.item.id)} style={{ color: 'var(--danger)' }}>✕</button>
                  </div>
                </div>
              ))}
            </div>

            <div className="card mt-md" style={{ padding: 20 }}>
              <div className="flex-between mb-md">
                <span>Subtotal</span>
                <span>₹{subtotal}</span>
              </div>
              <div className="flex-between mb-md">
                <span>Delivery fee {subtotal >= 500 && <span className="badge badge-success" style={{ marginLeft: 8 }}>Free</span>}</span>
                <span>₹{deliveryFee}</span>
              </div>
              <div className="flex-between" style={{ fontWeight: 700, fontSize: 18, paddingTop: 12, borderTop: '1px solid var(--border)' }}>
                <span>Total</span>
                <span>₹{total}</span>
              </div>
            </div>

            <div className="flex gap-md mt-lg">
              <button className="btn btn-secondary" onClick={clearCart}>Clear Cart</button>
              <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => setShowCheckout(true)}>
                Proceed to Checkout
              </button>
            </div>
          </>
        )}
      </div>

      {showCheckout && (
        <div className="modal-overlay" onClick={() => setShowCheckout(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Checkout</h2>
              <button className="btn btn-ghost" onClick={() => setShowCheckout(false)}>✕</button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>Your Name</label>
                <input type="text" value={name} onChange={e => setName(e.target.value)} placeholder="Enter your name" />
              </div>
              <div className="form-group">
                <label>Delivery Address</label>
                <textarea value={address} onChange={e => setAddress(e.target.value)} placeholder="Full delivery address" />
              </div>
              <div className="form-group">
                <label>Payment Method (Simulated)</label>
                <select value={payment} onChange={e => setPayment(e.target.value)}>
                  <option value="CASH">Cash on Delivery</option>
                </select>
              </div>
              <div className="card" style={{ padding: 16, background: 'var(--surface-alt)' }}>
                <div className="flex-between"><span>Subtotal</span><span>₹{subtotal}</span></div>
                <div className="flex-between"><span>Delivery</span><span>₹{deliveryFee}</span></div>
                <div className="flex-between" style={{ fontWeight: 700, marginTop: 8, paddingTop: 8, borderTop: '1px solid var(--border)' }}>
                  <span>Total</span><span>₹{total}</span>
                </div>
              </div>
              {error && <div className="form-error mt-md">{error}</div>}
              <p style={{ fontSize: 13, color: 'var(--muted)', marginTop: 12 }}>
                Payment is simulated. No real transaction will occur.
              </p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowCheckout(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleCheckout}>Place Order</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
