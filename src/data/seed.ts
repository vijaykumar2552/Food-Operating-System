// Provides demo catalog and order records for the root frontend data adapter.
// These local fixtures do not replace the intended Java API and MySQL data source.

import type { Restaurant, MenuItem, Category, Offer, Order } from '../types';

export function seedRestaurants(): Restaurant[] {
  const now = Date.now();
  return [
    {
      id: 'rest-1',
      name: 'Blue Basil Kitchen',
      description: 'Authentic Indian cuisine with a modern twist. Known for signature bowls and flavorful starters.',
      cuisine: 'Indian',
      address: '42 MG Road, Downtown, Bengaluru 560001',
      contactPhone: '+91 80 2234 5678',
      openingHours: '11:00 AM – 11:00 PM',
      rating: 4.7,
      deliveryTimeMinutes: 30,
      deliveryDistanceKm: 3.5,
      deliveryCharge: 40,
      status: 'OPEN',
      coverImageId: null,
      galleryImageIds: [],
      mapAddress: '42 MG Road, Downtown, Bengaluru 560001',
      mapLat: 12.9756,
      mapLng: 77.6066,
      mapLink: '',
      createdAt: now,
      updatedAt: now,
    },
    {
      id: 'rest-2',
      name: 'Spice Route',
      description: 'North Indian flavors with rich curries, fresh breads, and aromatic rice dishes.',
      cuisine: 'North Indian',
      address: '100 Feet Road, Indiranagar, Bengaluru 560038',
      contactPhone: '+91 80 4156 7890',
      openingHours: '12:00 PM – 10:30 PM',
      rating: 4.5,
      deliveryTimeMinutes: 35,
      deliveryDistanceKm: 4.2,
      deliveryCharge: 40,
      status: 'OPEN',
      coverImageId: null,
      galleryImageIds: [],
      mapAddress: '100 Feet Road, Indiranagar, Bengaluru 560038',
      mapLat: 12.9784,
      mapLng: 77.6408,
      mapLink: '',
      createdAt: now,
      updatedAt: now,
    },
    {
      id: 'rest-3',
      name: 'Pasta Lane',
      description: 'Wood-fired pizzas, fresh pastas, and classic Italian comfort food.',
      cuisine: 'Italian',
      address: '5th Block, Koramangala, Bengaluru 560095',
      contactPhone: '+91 80 2687 1234',
      openingHours: '11:30 AM – 11:00 PM',
      rating: 4.3,
      deliveryTimeMinutes: 40,
      deliveryDistanceKm: 5.0,
      deliveryCharge: 50,
      status: 'OPEN',
      coverImageId: null,
      galleryImageIds: [],
      mapAddress: '5th Block, Koramangala, Bengaluru 560095',
      mapLat: 12.9352,
      mapLng: 77.6245,
      mapLink: '',
      createdAt: now,
      updatedAt: now,
    },
    {
      id: 'rest-4',
      name: 'Dragon Wok',
      description: 'Wok-tossed noodles, fried rice, and bold Chinese flavors made fresh to order.',
      cuisine: 'Chinese',
      address: 'ITPL Road, Whitefield, Bengaluru 560066',
      contactPhone: '+91 80 3012 3456',
      openingHours: '12:00 PM – 10:00 PM',
      rating: 4.1,
      deliveryTimeMinutes: 45,
      deliveryDistanceKm: 6.8,
      deliveryCharge: 50,
      status: 'OPEN',
      coverImageId: null,
      galleryImageIds: [],
      mapAddress: 'ITPL Road, Whitefield, Bengaluru 560066',
      mapLat: 12.9698,
      mapLng: 77.7500,
      mapLink: '',
      createdAt: now,
      updatedAt: now,
    },
  ];
}

export function seedCategories(): Category[] {
  const now = Date.now();
  return [
    { id: 'cat-1', name: 'Signature Bowls', restaurantId: 'rest-1', sortOrder: 1, createdAt: now },
    { id: 'cat-2', name: 'Starters', restaurantId: 'rest-1', sortOrder: 2, createdAt: now },
    { id: 'cat-3', name: 'Mains', restaurantId: 'rest-2', sortOrder: 1, createdAt: now },
    { id: 'cat-4', name: 'Breads', restaurantId: 'rest-2', sortOrder: 2, createdAt: now },
    { id: 'cat-5', name: 'Pasta', restaurantId: 'rest-3', sortOrder: 1, createdAt: now },
    { id: 'cat-6', name: 'Pizza', restaurantId: 'rest-3', sortOrder: 2, createdAt: now },
    { id: 'cat-7', name: 'Noodles', restaurantId: 'rest-4', sortOrder: 1, createdAt: now },
    { id: 'cat-8', name: 'Rice', restaurantId: 'rest-4', sortOrder: 2, createdAt: now },
  ];
}

export function seedMenuItems(): MenuItem[] {
  const now = Date.now();
  return [
    { id: 'mi-1', restaurantId: 'rest-1', name: 'Paneer Tikka Bowl', description: 'Charred paneer, fragrant rice, mint yogurt and pickled onions.', categoryId: 'cat-1', price: 249, discountPrice: null, preparationTimeMinutes: 15, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-2', restaurantId: 'rest-1', name: 'Butter Chicken Bowl', description: 'Creamy tomato curry, basmati rice and coriander.', categoryId: 'cat-1', price: 289, discountPrice: null, preparationTimeMinutes: 18, isVeg: false, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-3', restaurantId: 'rest-1', name: 'Veg Biryani Bowl', description: 'Spiced basmati rice with mixed vegetables and saffron.', categoryId: 'cat-1', price: 219, discountPrice: 199, preparationTimeMinutes: 20, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-4', restaurantId: 'rest-1', name: 'Chilli Paneer', description: 'Crispy paneer tossed in spicy chilli sauce.', categoryId: 'cat-2', price: 189, discountPrice: null, preparationTimeMinutes: 12, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-5', restaurantId: 'rest-1', name: 'Spring Rolls', description: 'Golden rolls with vegetable filling.', categoryId: 'cat-2', price: 149, discountPrice: null, preparationTimeMinutes: 10, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-6', restaurantId: 'rest-2', name: 'Dal Makhani', description: 'Slow-cooked black lentils in a creamy tomato gravy.', categoryId: 'cat-3', price: 259, discountPrice: null, preparationTimeMinutes: 20, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-7', restaurantId: 'rest-2', name: 'Kadai Paneer', description: 'Paneer cooked with bell peppers in a spicy kadai gravy.', categoryId: 'cat-3', price: 279, discountPrice: 249, preparationTimeMinutes: 15, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-8', restaurantId: 'rest-2', name: 'Chole Kulche', description: 'Punjabi-style chickpea curry served with stuffed bread.', categoryId: 'cat-3', price: 199, discountPrice: null, preparationTimeMinutes: 15, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-9', restaurantId: 'rest-2', name: 'Garlic Naan', description: 'Soft tandoor-baked bread with garlic and butter.', categoryId: 'cat-4', price: 59, discountPrice: null, preparationTimeMinutes: 8, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-10', restaurantId: 'rest-2', name: 'Tandoori Roti', description: 'Whole wheat bread baked in a clay oven.', categoryId: 'cat-4', price: 39, discountPrice: null, preparationTimeMinutes: 6, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-11', restaurantId: 'rest-3', name: 'Margherita Pasta', description: 'Penne in a fresh basil and tomato sauce.', categoryId: 'cat-5', price: 229, discountPrice: null, preparationTimeMinutes: 15, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-12', restaurantId: 'rest-3', name: 'Alfredo Pasta', description: 'Creamy white sauce pasta with herbs.', categoryId: 'cat-5', price: 249, discountPrice: null, preparationTimeMinutes: 15, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-13', restaurantId: 'rest-3', name: 'Margherita Pizza', description: 'Classic pizza with mozzarella and basil.', categoryId: 'cat-6', price: 299, discountPrice: 269, preparationTimeMinutes: 20, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-14', restaurantId: 'rest-3', name: 'Farmhouse Pizza', description: 'Loaded with bell peppers, onions and mushrooms.', categoryId: 'cat-6', price: 349, discountPrice: null, preparationTimeMinutes: 22, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-15', restaurantId: 'rest-4', name: 'Hakka Noodles', description: 'Stir-fried noodles with vegetables and soy sauce.', categoryId: 'cat-7', price: 179, discountPrice: null, preparationTimeMinutes: 12, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-16', restaurantId: 'rest-4', name: 'Schezwan Noodles', description: 'Spicy schezwan sauce noodles with vegetables.', categoryId: 'cat-7', price: 199, discountPrice: null, preparationTimeMinutes: 12, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-17', restaurantId: 'rest-4', name: 'Veg Fried Rice', description: 'Wok-tossed rice with vegetables and spices.', categoryId: 'cat-8', price: 169, discountPrice: null, preparationTimeMinutes: 12, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
    { id: 'mi-18', restaurantId: 'rest-4', name: 'Schezwan Fried Rice', description: 'Spicy fried rice with schezwan sauce.', categoryId: 'cat-8', price: 189, discountPrice: 169, preparationTimeMinutes: 12, isVeg: true, availability: 'AVAILABLE', imageId: null, createdAt: now, updatedAt: now },
  ];
}

export function seedOffers(): Offer[] {
  const now = Date.now();
  const weekLater = now + 7 * 24 * 60 * 60 * 1000;
  return [
    { id: 'offer-1', title: 'Weekend Special', description: '10% off on all orders above ₹500', discountPercent: 10, validUntil: weekLater, imageId: null, createdAt: now },
    { id: 'offer-2', title: 'First Order Discount', description: '15% off on your first order (max ₹100)', discountPercent: 15, validUntil: weekLater, imageId: null, createdAt: now },
  ];
}

export function seedOrders(): Order[] {
  return [];
}
