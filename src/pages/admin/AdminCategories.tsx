// Manages menu categories in the root frontend admin area.
// React state and API operations drive category loading and edits.

import { useEffect, useState } from 'react'
import type { Category, Restaurant } from '../../types'
import { getCategories, getRestaurants, saveCategory, deleteCategory } from '../../lib/db'

interface Props { showToast: (msg: string) => void }

export default function AdminCategories({ showToast }: Props) {
  const [categories, setCategories] = useState<Category[]>([])
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState<Category | null>(null)

  useEffect(() => { load() }, [])
  const load = () => {
    Promise.all([getCategories(), getRestaurants()]).then(([c, r]) => {
      setCategories(c.sort((a, b) => a.sortOrder - b.sortOrder)); setRestaurants(r); setLoading(false)
    })
  }

  const restName = (id: string | null) => id ? restaurants.find(r => r.id === id)?.name || 'Unknown' : 'All restaurants'

  const startAdd = () => setEditing({ id: 'cat-' + Date.now(), name: '', restaurantId: restaurants[0]?.id || null, sortOrder: categories.length + 1, createdAt: Date.now() })
  const startEdit = (c: Category) => setEditing({ ...c })

  const handleSave = async () => {
    if (!editing) return
    if (!editing.name.trim()) { showToast('Category name is required'); return }
    await saveCategory(editing); setEditing(null); load()
    showToast('Category saved')
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this category?')) return
    await deleteCategory(id); load()
    showToast('Category deleted')
  }

  const moveUp = async (c: Category) => {
    const sorted = [...categories].sort((a, b) => a.sortOrder - b.sortOrder)
    const idx = sorted.findIndex(x => x.id === c.id)
    if (idx <= 0) return
    const prev = sorted[idx - 1]
    await saveCategory({ ...c, sortOrder: prev.sortOrder })
    await saveCategory({ ...prev, sortOrder: c.sortOrder })
    load()
  }

  const moveDown = async (c: Category) => {
    const sorted = [...categories].sort((a, b) => a.sortOrder - b.sortOrder)
    const idx = sorted.findIndex(x => x.id === c.id)
    if (idx >= sorted.length - 1) return
    const next = sorted[idx + 1]
    await saveCategory({ ...c, sortOrder: next.sortOrder })
    await saveCategory({ ...next, sortOrder: c.sortOrder })
    load()
  }

  if (loading) return <div className="loading-center"><div className="spinner" /></div>

  return (
    <>
      <div className="page-header">
        <div className="flex-between"><div><h1>Categories</h1><p>Create, reorder, and delete food categories</p></div>
          <button className="btn btn-primary" onClick={startAdd}>+ Add Category</button></div>
      </div>
      <div className="page-body">
        <div className="table-wrap">
          <table>
            <thead><tr><th>Order</th><th>Name</th><th>Restaurant</th><th>Actions</th></tr></thead>
            <tbody>
              {categories.sort((a, b) => a.sortOrder - b.sortOrder).map((c, i, arr) => (
                <tr key={c.id}>
                  <td className="text-muted">#{c.sortOrder}</td>
                  <td className="fw-600">{c.name}</td>
                  <td>{restName(c.restaurantId)}</td>
                  <td><div className="flex gap-sm">
                    <button className="btn btn-secondary btn-sm" disabled={i === 0} onClick={() => moveUp(c)}>↑</button>
                    <button className="btn btn-secondary btn-sm" disabled={i === arr.length - 1} onClick={() => moveDown(c)}>↓</button>
                    <button className="btn btn-secondary btn-sm" onClick={() => startEdit(c)}>Edit</button>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(c.id)}>Delete</button>
                  </div></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {editing && (
        <div className="modal-overlay" onClick={() => setEditing(null)}>
          <div className="modal" style={{ maxWidth: 460 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h2>{editing.name ? 'Edit' : 'Add'} Category</h2><button className="btn btn-ghost" onClick={() => setEditing(null)}>✕</button></div>
            <div className="modal-body">
              <div className="form-group"><label>Name *</label><input value={editing.name} onChange={e => setEditing({ ...editing, name: e.target.value })} /></div>
              <div className="form-group"><label>Restaurant</label>
                <select value={editing.restaurantId || ''} onChange={e => setEditing({ ...editing, restaurantId: e.target.value || null })}>
                  {restaurants.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
                </select>
              </div>
              <div className="form-group"><label>Sort Order</label><input type="number" value={editing.sortOrder} onChange={e => setEditing({ ...editing, sortOrder: parseInt(e.target.value) || 0 })} /></div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setEditing(null)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave}>Save</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
