// Composes the root frontend admin shell and its management views.
// React state and navigation coordinate admin actions across the page components.

import { Routes, Route, NavLink, Navigate, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import AdminOverview from './AdminOverview'
import AdminRestaurants from './AdminRestaurants'
import AdminMenu from './AdminMenu'
import AdminOrders from './AdminOrders'
import AdminSettings from './AdminSettings'
import { currentUser, api } from '../../lib/api'

const NAV = [
  { path: 'overview', label: 'Overview' },
  { path: 'restaurants', label: 'Restaurants' },
  { path: 'menu', label: 'Food Menu' },
  { path: 'orders', label: 'Orders' },
  { path: 'settings', label: 'Settings' },
]

export default function AdminApp() {
  const navigate = useNavigate()
  const [toast, setToast] = useState<string | null>(null)

  useEffect(() => {
    if (!toast) return
    const t = setTimeout(() => setToast(null), 2500)
    return () => clearTimeout(t)
  }, [toast])

  const showToast = (msg: string) => setToast(msg)

  const user = currentUser()
  if (!user || user.role !== 'ADMIN') return <Navigate to="/settings" replace />

  const pageProps = { showToast }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-brand">Food<span>Flow</span></div>
          <div style={{ fontSize: 12, color: 'var(--muted)', marginTop: 4 }}>Admin Panel</div>
        </div>
        <nav className="sidebar-nav">
          {NAV.map(item => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button className="btn btn-secondary btn-sm w-full" onClick={() => api.logout().then(() => navigate('/settings'))} style={{ marginBottom: 8, background: 'rgba(255,255,255,0.08)', color: '#CBD5E1', border: 'none' }}>Log out</button>
          <button
            className="btn btn-sm w-full"
            onClick={() => navigate('/')}
            style={{ background: 'transparent', color: '#94A3B8' }}
          >
            ← Back to Customer View
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Routes>
          <Route path="overview" element={<AdminOverview {...pageProps} />} />
          <Route path="restaurants" element={<AdminRestaurants {...pageProps} />} />
          <Route path="menu" element={<AdminMenu {...pageProps} />} />
          <Route path="orders" element={<AdminOrders {...pageProps} />} />
          <Route path="settings" element={<AdminSettings {...pageProps} />} />
          <Route path="*" element={<Navigate to="overview" />} />
        </Routes>
      </main>

      {toast && <div className="toast">{toast}</div>}
    </div>
  )
}
