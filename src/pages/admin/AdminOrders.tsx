// Lists customer orders and submits status updates from the root admin UI.
// React effects load orders while event handlers persist admin changes through the API.

import { useEffect, useState } from 'react'
import { api, type ApiAdminOrder } from '../../lib/api'
interface Props { showToast: (message: string) => void }
const STATUSES = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'REJECTED', 'CANCELLED']
export default function AdminOrders({ showToast }: Props) {
  const [orders, setOrders] = useState<ApiAdminOrder[]>([]); const [error, setError] = useState('')
  const load = () => api.adminOrders().then(setOrders).catch(caught => setError(caught.message))
  useEffect(() => { load() }, [])
  const update = async (id: number, status: string) => { try { await api.updateOrderStatus(String(id), status); await load(); showToast('Order status updated.') } catch (caught) { setError(caught instanceof Error ? caught.message : 'Update failed.') } }
  return <><div className="page-header"><h1>Orders</h1><p>View and update MySQL customer orders</p></div><div className="page-body">{error && <div className="form-error">{error}</div>}<div className="table-wrap"><table><thead><tr><th>Order</th><th>Customer</th><th>Restaurant</th><th>Total</th><th>Date</th><th>Status</th></tr></thead><tbody>{orders.map(order => <tr key={order.id}><td>#{order.id}</td><td>{order.customerId}</td><td>{order.restaurantName}</td><td>₹{order.totalAmount}</td><td>{new Date(order.orderDate).toLocaleString()}</td><td><select value={order.status} onChange={e => update(order.id, e.target.value)}>{STATUSES.map(status => <option key={status}>{status}</option>)}</select></td></tr>)}</tbody></table></div></div></>
}
