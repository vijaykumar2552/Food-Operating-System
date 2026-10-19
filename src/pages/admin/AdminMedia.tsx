// Manages restaurant and menu media in the root frontend admin area.
// React state connects selected media records to the local data adapter.

import { useEffect, useState } from 'react'
import type { StoredImage } from '../../types'
import { getImage, deleteImage } from '../../lib/db'
import { getRestaurants, getMenuItems, getOffers } from '../../lib/db'

interface Props { showToast: (msg: string) => void }

export default function AdminMedia({ showToast }: Props) {
  const [images, setImages] = useState<{ id: string; blob: Blob; url: string; type: string }[]>([])
  const [loading, setLoading] = useState(true)
  const [usedIds, setUsedIds] = useState<Set<string>>(new Set())

  useEffect(() => { load() }, [])
  const load = async () => {
    const db = await indexedDB.open('foodflow', 1)
    db.onsuccess = () => {
      const tx = db.result.transaction('images', 'readonly')
      const req = tx.objectStore('images').getAll()
      req.onsuccess = async () => {
        const imgs = (req.result as StoredImage[]).map(img => ({
          id: img.id, blob: img.blob, url: URL.createObjectURL(img.blob), type: img.type
        }))
        setImages(imgs)
        const [r, m, o] = await Promise.all([getRestaurants(), getMenuItems(), getOffers()])
        const ids = new Set<string>()
        r.forEach(x => { if (x.coverImageId) ids.add(x.coverImageId); x.galleryImageIds.forEach(id => ids.add(id)) })
        m.forEach(x => { if (x.imageId) ids.add(x.imageId) })
        o.forEach(x => { if (x.imageId) ids.add(x.imageId) })
        setUsedIds(ids)
        setLoading(false)
      }
    }
  }

  const handleDelete = async (id: string) => {
    if (usedIds.has(id)) { showToast('This image is in use. Remove it from the restaurant/menu/offer first.'); return }
    await deleteImage(id)
    load()
    showToast('Image deleted')
  }

  if (loading) return <div className="loading-center"><div className="spinner" /></div>

  return (
    <>
      <div className="page-header"><h1>Photos & Media</h1><p>View and manage all uploaded images</p></div>
      <div className="page-body">
        {images.length === 0 ? (
          <div className="empty-state"><div className="icon">🖼️</div><p>No images uploaded yet. Upload images from the Restaurants, Food Menu, or Offers pages.</p></div>
        ) : (
          <>
            <p className="text-muted mb-md">{images.length} image(s) stored. Images in use cannot be deleted until removed from their item.</p>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16 }}>
              {images.map(img => (
                <div key={img.id} className="card" style={{ padding: 12 }}>
                  <img src={img.url} alt="" style={{ width: '100%', height: 140, objectFit: 'cover', borderRadius: 6 }} />
                  <div className="flex-between mt-md">
                    <span className="text-muted" style={{ fontSize: 12 }}>{img.type.split('/')[1]} · {(img.blob.size / 1024).toFixed(0)} KB</span>
                    {usedIds.has(img.id) ? (
                      <span className="badge badge-info">In use</span>
                    ) : (
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(img.id)}>Delete</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </>
  )
}
