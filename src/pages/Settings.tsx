// Renders customer account settings in the root frontend.
// React state and form events manage profile details and user preferences.

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, clearSession, currentUser } from '../lib/api'

export default function Settings() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const user = currentUser()
  const submit = async () => {
    try {
      setError(''); setMessage('')
      if (mode === 'register') {
        await api.register({ fullName: name, email, password, phone })
        setMessage('Registration complete. You can now log in.')
        setMode('login')
      } else {
        const loggedIn = await api.login(email, password)
        if (loggedIn.role === 'ADMIN') navigate('/admin')
        else window.location.reload()
      }
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'Request failed.') }
  }
  const logout = async () => { await api.logout(); clearSession(); window.location.reload() }
  return <>
    <header className="customer-header"><Link to="/" className="brand">Food<span>Flow</span></Link><nav><Link to="/" className="btn btn-ghost btn-sm">← Back</Link></nav></header>
    <div style={{ maxWidth: 600, margin: '0 auto', padding: 24 }}>
      <h1 style={{ marginBottom: 24 }}>Account</h1>
      {user ? <div className="card" style={{ padding: 24 }}><h3>Welcome, {user.fullName}</h3><p className="text-muted">{user.email} · {user.role}</p>{user.role === 'ADMIN' && <button className="btn btn-primary" onClick={() => navigate('/admin')}>Open Admin Panel</button>}<button className="btn btn-secondary" onClick={logout} style={{ marginLeft: 8 }}>Log out</button></div> :
        <div className="card" style={{ padding: 24 }}><div className="flex gap-md mb-md"><button className={`btn ${mode === 'login' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setMode('login')}>Login</button><button className={`btn ${mode === 'register' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setMode('register')}>Register</button></div>
          {mode === 'register' && <div className="form-group"><label>Full name</label><input value={name} onChange={event => setName(event.target.value)} /></div>}
          <div className="form-group"><label>Email</label><input type="email" value={email} onChange={event => setEmail(event.target.value)} /></div>
          {mode === 'register' && <div className="form-group"><label>Phone</label><input value={phone} onChange={event => setPhone(event.target.value)} /></div>}
          <div className="form-group"><label>Password</label><input type="password" value={password} onChange={event => setPassword(event.target.value)} onKeyDown={event => event.key === 'Enter' && submit()} /></div>
          {error && <div className="form-error">{error}</div>}{message && <p>{message}</p>}<button className="btn btn-primary" onClick={submit}>{mode === 'login' ? 'Log in' : 'Create account'}</button>
        </div>}
    </div>
  </>
}
