// Manages restaurant menu items in the root frontend admin area.
// React state and API operations support menu edits and availability changes.

import { useEffect, useState } from 'react'
import { api, type ApiMenuItem } from '../../lib/api'
import type { Restaurant } from '../../types'
interface Props { showToast: (message: string) => void }
export default function AdminMenu({ showToast }: Props) {
  const [items, setItems] = useState<ApiMenuItem[]>([])
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [categories, setCategories] = useState<Array<{ id: number; restaurantId: number; name: string; sortOrder: number }>>([])
  const [selectedRestaurantId, setSelectedRestaurantId] = useState('')
  const [editing, setEditing] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = async (restaurantId = selectedRestaurantId) => {
    try {
      setError('')
      const [restaurantValues, categoryValues] = await Promise.all([api.adminRestaurants(), api.adminCategories()])
      const nextRestaurantId = restaurantId || restaurantValues[0]?.id || ''
      setRestaurants(restaurantValues)
      setCategories(categoryValues)
      setSelectedRestaurantId(nextRestaurantId)
      setItems(nextRestaurantId ? await api.adminMenu(nextRestaurantId) : [])
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Menu could not be loaded.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const selectedCategories = categories.filter(category => String(category.restaurantId) === selectedRestaurantId)
  const save = async () => {
    try {
      if (!editing.name?.trim() || !editing.categoryId || !editing.price || !selectedRestaurantId) throw new Error('Restaurant, name, category, and price are required.')
      if (editing.id) await api.updateMenuItem(editing.id, editing)
      else await api.createMenuItem(editing)
      setEditing(null)
      await load(selectedRestaurantId)
      showToast('Menu item saved.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Save failed.')
    }
  }
  const remove = async (id: string) => {
    if (!confirm('Delete this menu item?')) return
    try {
      await api.deleteMenuItem(id)
      await load(selectedRestaurantId)
      showToast('Menu item deleted.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Delete failed.')
    }
  }
  const startAdd = () => {
    if (!selectedCategories.length) {
      setError('Create a category for this restaurant before adding a menu item.')
      return
    }
    setEditing({ name: '', description: '', categoryId: selectedCategories[0].id, price: 0, discountPrice: null, preparationTimeMinutes: 15, isVeg: true, availability: 'AVAILABLE' })
  }
  const changeRestaurant = (restaurantId: string) => {
    setSelectedRestaurantId(restaurantId)
    load(restaurantId)
  }

  if (loading) return <div className="loading-center"><div className="spinner" /></div>
  return <>
    <div className="page-header"><div className="flex-between"><div><h1>Food Menu</h1><p>Manage MySQL menu items for the selected restaurant</p></div><button className="btn btn-primary" onClick={startAdd}>+ Add Menu Item</button></div></div>
    <div className="page-body">
      {error && <div className="form-error">{error}</div>}
      <div className="form-group" style={{ maxWidth: 420 }}><label>Restaurant</label><select value={selectedRestaurantId} onChange={event => changeRestaurant(event.target.value)}>{restaurants.map(restaurant => <option key={restaurant.id} value={restaurant.id}>{restaurant.name}</option>)}</select></div>
      <div className="table-wrap"><table><thead><tr><th>Item</th><th>Category</th><th>Price</th><th>Availability</th><th>Actions</th></tr></thead><tbody>{items.map(item => <tr key={item.id}><td>{item.name}</td><td>{item.category}</td><td>₹{item.price}</td><td>{item.availability}</td><td><button className="btn btn-secondary btn-sm" onClick={() => setEditing({ id: item.id, name: item.name, description: item.description, categoryId: item.categoryId, price: item.price, discountPrice: item.discountPrice, preparationTimeMinutes: item.preparationTimeMinutes, isVeg: item.isVeg, availability: item.availability })}>Edit</button> <button className="btn btn-danger btn-sm" onClick={() => remove(String(item.id))}>Delete</button></td></tr>)}</tbody></table>{!items.length && <div className="empty-state"><p>No menu items found for this restaurant.</p></div>}</div>
    </div>
    {editing && <div className="modal-overlay"><div className="modal"><div className="modal-header"><h2>{editing.id ? 'Edit' : 'Add'} Menu Item</h2></div><div className="modal-body"><div className="form-group"><label>Name</label><input value={editing.name} onChange={e => setEditing({ ...editing, name: e.target.value })} /></div><div className="form-group"><label>Category</label><select value={editing.categoryId} onChange={e => setEditing({ ...editing, categoryId: Number(e.target.value) })}>{selectedCategories.map(category => <option key={category.id} value={category.id}>{category.name}</option>)}</select></div><div className="form-group"><label>Description</label><textarea value={editing.description} onChange={e => setEditing({ ...editing, description: e.target.value })} /></div><div className="form-group"><label>Price</label><input type="number" value={editing.price} onChange={e => setEditing({ ...editing, price: Number(e.target.value) })} /></div><div className="form-group"><label>Availability</label><select value={editing.availability} onChange={e => setEditing({ ...editing, availability: e.target.value })}><option>AVAILABLE</option><option>SOLD_OUT</option><option>HIDDEN</option></select></div></div><div className="modal-footer"><button className="btn btn-secondary" onClick={() => setEditing(null)}>Cancel</button><button className="btn btn-primary" onClick={save}>Save</button></div></div></div>}
  </>
}
