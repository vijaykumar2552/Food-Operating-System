// Implements the root frontend IndexedDB adapter for catalog and order data.
// IndexedDB transactions and seed records support this separate browser-based client.

import type { Restaurant, MenuItem, Category, Offer, Order, StoredImage } from '../types';
import { seedRestaurants, seedCategories, seedMenuItems, seedOffers, seedOrders } from '../data/seed';

const DB_NAME = 'foodflow';
const DB_VERSION = 1;
const STORES = ['restaurants', 'menuItems', 'categories', 'offers', 'orders', 'images', 'meta'];

let dbPromise: Promise<IDBDatabase> | null = null;

function openDB(): Promise<IDBDatabase> {
  if (dbPromise) return dbPromise;
  dbPromise = new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onerror = () => reject(req.error);
    req.onsuccess = () => resolve(req.result);
    req.onupgradeneeded = () => {
      const db = req.result;
      for (const store of STORES) {
        if (!db.objectStoreNames.contains(store)) {
          db.createObjectStore(store, { keyPath: 'id' });
        }
      }
    };
  });
  return dbPromise;
}

async function tx<T>(store: string, mode: IDBTransactionMode, fn: (s: IDBObjectStore) => IDBRequest<T>): Promise<T> {
  const db = await openDB();
  return new Promise((resolve, reject) => {
    const t = db.transaction(store, mode);
    const req = fn(t.objectStore(store));
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function getAll<T>(store: string): Promise<T[]> {
  return tx<T[]>(store, 'readonly', s => s.getAll() as IDBRequest<T[]>);
}

async function getOne<T>(store: string, id: string): Promise<T | undefined> {
  return tx<T | undefined>(store, 'readonly', s => s.get(id) as IDBRequest<T | undefined>);
}

async function put<T>(store: string, value: T): Promise<void> {
  await tx(store, 'readwrite', s => s.put(value as any));
}

async function putMany<T>(store: string, values: T[]): Promise<void> {
  const db = await openDB();
  return new Promise((resolve, reject) => {
    const t = db.transaction(store, 'readwrite');
    const s = t.objectStore(store);
    for (const v of values) s.put(v as any);
    t.oncomplete = () => resolve();
    t.onerror = () => reject(t.error);
  });
}

async function remove(store: string, id: string): Promise<void> {
  await tx(store, 'readwrite', s => s.delete(id));
}

async function clearStore(store: string): Promise<void> {
  await tx(store, 'readwrite', s => s.clear());
}

async function getMeta(key: string): Promise<any> {
  const r = await getOne<any>('meta', key);
  return r;
}

async function setMeta(key: string, value: any): Promise<void> {
  await put('meta', { id: key, ...value });
}

// ─── Public API ───

export async function isSeeded(): Promise<boolean> {
  return (await getMeta('seeded'))?.value === true;
}

export async function ensureSeeded(): Promise<void> {
  if (await isSeeded()) return;
  await putMany('restaurants', seedRestaurants());
  await putMany('categories', seedCategories());
  await putMany('menuItems', seedMenuItems());
  await putMany('offers', seedOffers());
  await putMany('orders', seedOrders());
  await setMeta('seeded', { value: true });
}

export async function resetToSeed(): Promise<void> {
  await clearStore('restaurants');
  await clearStore('categories');
  await clearStore('menuItems');
  await clearStore('offers');
  await clearStore('orders');
  await clearStore('images');
  await putMany('restaurants', seedRestaurants());
  await putMany('categories', seedCategories());
  await putMany('menuItems', seedMenuItems());
  await putMany('offers', seedOffers());
  await putMany('orders', seedOrders());
  await setMeta('seeded', { value: true });
}

// Restaurants
export const getRestaurants = () => getAll<Restaurant>('restaurants');
export const getRestaurant = (id: string) => getOne<Restaurant>('restaurants', id);
export const saveRestaurant = (r: Restaurant) => put('restaurants', r);
export const deleteRestaurant = (id: string) => remove('restaurants', id);

// Menu Items
export const getMenuItems = () => getAll<MenuItem>('menuItems');
export const getMenuItem = (id: string) => getOne<MenuItem>('menuItems', id);
export const getMenuItemsByRestaurant = async (restaurantId: string) =>
  (await getAll<MenuItem>('menuItems')).filter(m => m.restaurantId === restaurantId);
export const saveMenuItem = (m: MenuItem) => put('menuItems', m);
export const deleteMenuItem = (id: string) => remove('menuItems', id);

// Categories
export const getCategories = () => getAll<Category>('categories');
export const getCategory = (id: string) => getOne<Category>('categories', id);
export const saveCategory = (c: Category) => put('categories', c);
export const deleteCategory = (id: string) => remove('categories', id);

// Offers
export const getOffers = () => getAll<Offer>('offers');
export const saveOffer = (o: Offer) => put('offers', o);
export const deleteOffer = (id: string) => remove('offers', id);

// Orders
export const getOrders = () => getAll<Order>('orders');
export const getOrder = (id: string) => getOne<Order>('orders', id);
export const saveOrder = (o: Order) => put('orders', o);
export const deleteOrder = (id: string) => remove('orders', id);

// Images
export const saveImage = async (blob: Blob): Promise<string> => {
  const id = 'img-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8);
  const stored: StoredImage = { id, blob, type: blob.type, createdAt: Date.now() };
  await put('images', stored);
  return id;
};

export const getImage = async (id: string): Promise<StoredImage | undefined> => {
  if (!id) return undefined;
  return getOne<StoredImage>('images', id);
};

export const imageUrl = async (id: string | null): Promise<string | null> => {
  if (!id) return null;
  const img = await getImage(id);
  if (!img) return null;
  return URL.createObjectURL(img.blob);
};

export const deleteImage = (id: string) => remove('images', id);
