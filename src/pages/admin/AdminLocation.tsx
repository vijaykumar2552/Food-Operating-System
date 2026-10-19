// Edits restaurant location details in the root frontend admin area.
// Form state and API operations keep location management within the admin workflow.

import { useEffect, useState } from 'react'
import type { Restaurant } from '../../types'
import { getRestaurants, saveRestaurant } from '../../lib/db'
import { useImageUrl } from '../../hooks/useImageUrl'

interface Props { showToast: (msg: string) => void }

export default function AdminLocation({ showToast }: Props) {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedId, setSelectedId] = useState<string>('')
  const [editing, setEditing] = useState<Restaurant | null>(null)

  useEffect(() => { load() }, [])
  const load = () => {
    getRestaurants().then(r => {
      setRestaurants(r)
      if (r.length > 0 && !selectedId) setSelectedId(r[0].id)
      setLoading(false)
    })
  }

  useEffect(() => {
    const r = restaurants.find(x => x.id === selectedId)
    if (r) setEditing({ ...r })
  }, [selectedId, restaurants])

  const handleSave = async () => {
    if (!editing) return
    await saveRestaurant({ ...editing, updatedAt: Date.now() })
    load()
    showToast('Location updated')
  }

  if (loading) return <div className="loading-center"><div className="spinner" /></div>

  const selected = restaurants.find(r => r.id === selectedId)
  const coverUrl = useImageUrl(selected?.coverImageId ?? null)

  return (
    <>
      <div className="page-header"><h1>Location & Map</h1><p>Edit restaurant addresses and map coordinates</p></div>
      <div className="page-body">
        <div className="form-group" style={{ maxWidth: 400 }}>
          <label>Select Restaurant</label>
          <select value={selectedId} onChange={e => setSelectedId(e.target.value)}>
            {restaurants.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
          </select>
        </div>

        {editing && (
          <div className="card mt-lg" style={{ maxWidth: 700 }}>
            <div className="flex gap-md mb-lg" style={{ alignItems: 'flex-start' }}>
              {coverUrl && <img src={coverUrl} alt="" style={{ width: 100, height: 100, borderRadius: 8, objectFit: 'cover' }} />}
              <div><h3>{editing.name}</h3><p className="text-muted">{editing.cuisine}</p></div>
            </div>
            <div className="form-grid">
              <div className="form-group full"><label>Map Address</label><input value={editing.mapAddress} onChange={e => setEditing({ ...editing, mapAddress: e.target.value, address: e.target.value || editing.address })} placeholder="Full street address" /></div>
              <div className="form-group"><label>Latitude</label><input type="number" step="any" value={editing.mapLat ?? ''} onChange={e => setEditing({ ...editing, mapLat: e.target.value ? parseFloat(e.target.value) : null })} placeholder="e.g. 12.9756" /></div>
              <div className="form-group"><label>Longitude</label><input type="number" step="any" value={editing.mapLng ?? ''} onChange={e => setEditing({ ...editing, mapLng: e.target.value ? parseFloat(e.target.value) : null })} placeholder="e.g. 77.6066" /></div>
              <div className="form-group full"><label>Map Link (optional)</label><input value={editing.mapLink} onChange={e => setEditing({ ...editing, mapLink: e.target.value })} placeholder="https://maps.google.com/..." /></div>
            </div>
            {editing.mapLat != null && editing.mapLng != null && (
              <div style={{ marginTop: 16, padding: 16, background: 'var(--surface-alt)', borderRadius: 8, fontSize: 14, color: 'var(--text-secondary)' }}>
                <strong>Coordinates preview:</strong> {editing.mapLat}, {editing.mapLng}
                <br /><span className="text-muted" style={{ fontSize: 13 }}>No interactive map is shown because no map provider API key is configured. The coordinates and map link will be displayed to customers in restaurant details.</span>
              </div>
            )}
            <div className="mt-lg"><button className="btn btn-primary" onClick={handleSave}>Save Location</button></div>
          </div>
        )}
      </div>
    </>
  )
}
