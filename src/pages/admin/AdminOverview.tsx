// Presents summary metrics for the root frontend admin dashboard.
// React state composes current order and catalog information into overview cards.

import { useEffect, useState } from 'react'
import { api } from '../../lib/api'
interface Props { showToast: (message: string) => void }
export default function AdminOverview(_: Props) {
  const [stats, setStats] = useState({ restaurants: 0, menuItems: 0, orders: 0 }); const [error, setError] = useState('')
  useEffect(() => { Promise.all([api.adminRestaurants(), api.adminMenu(), api.adminOrders()]).then(([restaurants, menu, orders]) => setStats({ restaurants: restaurants.length, menuItems: menu.length, orders: orders.length })).catch(caught => setError(caught.message)) }, [])
  return <><div className="page-header"><h1>Overview</h1><p>MySQL-backed FoodOS administration</p></div><div className="page-body">{error && <div className="form-error">{error}</div>}<div className="stat-grid"><div className="stat-card"><div className="label">Restaurants</div><div className="value">{stats.restaurants}</div></div><div className="stat-card"><div className="label">Menu Items</div><div className="value">{stats.menuItems}</div></div><div className="stat-card"><div className="label">Orders</div><div className="value">{stats.orders}</div></div></div></div></>
}
