// Presents configuration controls for the root frontend admin area.
// Component state keeps settings interactions separate from catalog management.

import { useNavigate } from 'react-router-dom'
import { api, currentUser } from '../../lib/api'
interface Props { showToast: (message: string) => void }
export default function AdminSettings({ showToast }: Props) {
  const navigate = useNavigate(); const user = currentUser()
  const logout = async () => { await api.logout(); showToast('Logged out.'); navigate('/settings') }
  return <><div className="page-header"><h1>Settings</h1><p>Server-side administrator session</p></div><div className="page-body"><div className="card" style={{ maxWidth: 600, padding: 24 }}><h3>{user?.fullName}</h3><p className="text-muted">{user?.email} · {user?.role}</p><p className="text-muted">Restaurant, menu, and order data is stored in MySQL.</p><button className="btn btn-secondary" onClick={logout}>Log out</button></div></div></>
}
