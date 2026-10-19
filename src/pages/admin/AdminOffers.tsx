// Manages promotional offers in the root frontend admin area.
// React forms and data operations keep offer editing separate from other admin views.

import { useEffect, useState } from 'react'
import type { Offer } from '../../types'
import { getOffers, saveOffer, deleteOffer, saveImage } from '../../lib/db'
import { useImageUrl } from '../../hooks/useImageUrl'

interface Props { showToast: (msg: string) => void }

const EMPTY: Offer = { id: '', title: '', description: '', discountPercent: 0, validUntil: Date.now() + 7 * 86400000, imageId: null, createdAt: Date.now() }

export default function AdminOffers({ showToast }: Props) {
  const [offers, setOffers] = useState<Offer[]>([])
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState<Offer | null>(null)
  const [imgPreview, setImgPreview] = useState<string | null>(null)

  useEffect(() => { load() }, [])
  const load = () => { getOffers().then(o => { setOffers(o); setLoading(false) }) }

  const startAdd = () => { setEditing({ ...EMPTY, id: 'offer-' + Date.now() }); setImgPreview(null) }
  const startEdit = (o: Offer) => { setEditing({ ...o }); setImgPreview(null) }

  const handleSave = async () => {
    if (!editing) return
    if (!editing.title.trim()) { showToast('Offer title is required'); return }
    await saveOffer(editing); setEditing(null); load()
    showToast('Offer saved')
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this offer?')) return
    await deleteOffer(id); load()
    showToast('Offer deleted')
  }

  const handleImageUpload = async (file: File) => {
    if (!file.type.startsWith('image/')) { showToast('Please select an image file'); return }
    if (file.size > 5 * 1024 * 1024) { showToast('Image must be under 5MB'); return }
    const id = await saveImage(file)
    if (editing) setEditing({ ...editing, imageId: id })
    setImgPreview(URL.createObjectURL(file))
    showToast('Offer image uploaded')
  }

  if (loading) return <div className="loading-center"><div className="spinner" /></div>

  return (
    <>
      <div className="page-header">
        <div className="flex-between"><div><h1>Offers & Discounts</h1><p>Create promotional offers and banners</p></div>
          <button className="btn btn-primary" onClick={startAdd}>+ Add Offer</button></div>
      </div>
      <div className="page-body">
        {offers.length === 0 ? (
          <div className="empty-state"><div className="icon">🏷️</div><p>No offers yet.</p></div>
        ) : (
          <div className="restaurant-grid">
            {offers.map(o => <OfferCard key={o.id} o={o} onEdit={() => startEdit(o)} onDelete={() => handleDelete(o.id)} />)}
          </div>
        )}
      </div>

      {editing && (
        <div className="modal-overlay" onClick={() => setEditing(null)}>
          <div className="modal" style={{ maxWidth: 560 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h2>{editing.title ? 'Edit' : 'Add'} Offer</h2><button className="btn btn-ghost" onClick={() => setEditing(null)}>✕</button></div>
            <div className="modal-body">
              <div className="form-grid">
                <div className="form-group full"><label>Title *</label><input value={editing.title} onChange={e => setEditing({ ...editing, title: e.target.value })} /></div>
                <div className="form-group full"><label>Description</label><textarea value={editing.description} onChange={e => setEditing({ ...editing, description: e.target.value })} /></div>
                <div className="form-group"><label>Discount %</label><input type="number" min="0" max="100" value={editing.discountPercent} onChange={e => setEditing({ ...editing, discountPercent: parseInt(e.target.value) || 0 })} /></div>
                <div className="form-group"><label>Valid Until</label><input type="date" value={new Date(editing.validUntil).toISOString().slice(0, 10)} onChange={e => setEditing({ ...editing, validUntil: new Date(e.target.value).getTime() })} /></div>
                <div className="form-group full"><label>Offer Image</label>
                  <div className="image-upload" onClick={() => document.getElementById('offer-img-upload')?.click()}>
                    {imgPreview ? <img src={imgPreview} alt="Preview" /> : 'Click to upload offer banner image (max 5MB)'}
                  </div>
                  <input id="offer-img-upload" type="file" accept="image/*" className="hidden" onChange={e => e.target.files?.[0] && handleImageUpload(e.target.files[0])} />
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setEditing(null)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave}>Save Offer</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}

function OfferCard({ o, onEdit, onDelete }: { o: Offer; onEdit: () => void; onDelete: () => void }) {
  const imgUrl = useImageUrl(o.imageId)
  const expired = o.validUntil < Date.now()
  return (
    <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
      <div style={{ height: 120, background: 'linear-gradient(135deg, #1E293B, #334155)' }}>
        {imgUrl && <img src={imgUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />}
      </div>
      <div style={{ padding: 16 }}>
        <h3>{o.title}</h3>
        <p className="text-muted" style={{ fontSize: 14, marginBottom: 8 }}>{o.description}</p>
        <div className="flex-between">
          <div>
            <span className="badge badge-info">{o.discountPercent}% off</span>
            <span className={`badge ${expired ? 'badge-danger' : 'badge-success'}`} style={{ marginLeft: 8 }}>
              {expired ? 'Expired' : 'Active'}
            </span>
          </div>
          <div className="flex gap-sm">
            <button className="btn btn-secondary btn-sm" onClick={onEdit}>Edit</button>
            <button className="btn btn-danger btn-sm" onClick={onDelete}>Delete</button>
          </div>
        </div>
      </div>
    </div>
  )
}
