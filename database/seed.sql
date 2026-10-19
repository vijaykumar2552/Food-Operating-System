-- Loads the original FoodOS demonstration users and catalog records.
-- This script adds seed records and does not replace application startup.

USE food_ordering;

-- ============================================================
-- Seed data — demo restaurants, menu items, accounts.
-- All passwords are BCrypt-hashed. The plaintext for every
-- account below is "password".
-- ============================================================

-- ── Admin account ──────────────────────────────────────────
INSERT INTO users (email, password_hash, full_name, role) VALUES
('admin@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Admin', 'ADMIN')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), role = 'ADMIN';

-- ── Restaurant owners (one per restaurant) ─────────────────
INSERT INTO users (email, password_hash, full_name, role) VALUES
('owner1@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Blue Basil Owner', 'RESTAURANT_OWNER'),
('owner2@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Spice Route Owner', 'RESTAURANT_OWNER'),
('owner3@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pasta Lane Owner', 'RESTAURANT_OWNER'),
('owner4@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dragon Wok Owner', 'RESTAURANT_OWNER')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), role = 'RESTAURANT_OWNER';

-- ── Staff account (legacy, for existing Swing UI) ──────────
INSERT INTO users (email, password_hash, full_name, role) VALUES
('staff@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Restaurant Staff', 'RESTAURANT_STAFF')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- ── Demo customer ──────────────────────────────────────────
INSERT INTO users (email, password_hash, full_name, role) VALUES
('customer@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo Customer', 'CUSTOMER')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

INSERT INTO customers (customer_id, phone)
SELECT user_id, '9876543210' FROM users WHERE email = 'customer@foodapp.local'
ON DUPLICATE KEY UPDATE phone = VALUES(phone);

-- ── Restaurant 1: Blue Basil Kitchen ───────────────────────
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, contact_phone, opening_hours,
                         rating, delivery_time_minutes, delivery_distance_km, delivery_charge, status, map_address, map_lat, map_lng)
SELECT user_id, 'Blue Basil Kitchen',
  'Authentic Indian cuisine with a modern twist. Known for signature bowls and flavorful starters.',
  'Indian', '42 MG Road, Downtown, Bengaluru 560001', '+91 80 2234 5678', '11:00 AM - 11:00 PM',
  4.70, 30, 3.5, 40.00, 'ACTIVE', '42 MG Road, Downtown, Bengaluru 560001', 12.9756, 77.6066
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE description = VALUES(description), status = 'ACTIVE', rating = 4.70;

-- ── Restaurant 2: Spice Route ──────────────────────────────
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, contact_phone, opening_hours,
                         rating, delivery_time_minutes, delivery_distance_km, delivery_charge, status, map_address, map_lat, map_lng)
SELECT user_id, 'Spice Route',
  'North Indian flavors with rich curries, fresh breads, and aromatic rice dishes.',
  'North Indian', '100 Feet Road, Indiranagar, Bengaluru 560038', '+91 80 4156 7890', '12:00 PM - 10:30 PM',
  4.50, 35, 4.2, 40.00, 'ACTIVE', '100 Feet Road, Indiranagar, Bengaluru 560038', 12.9784, 77.6408
FROM users WHERE email = 'owner2@foodapp.local'
ON DUPLICATE KEY UPDATE description = VALUES(description), status = 'ACTIVE', rating = 4.50;

-- ── Restaurant 3: Pasta Lane ───────────────────────────────
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, contact_phone, opening_hours,
                         rating, delivery_time_minutes, delivery_distance_km, delivery_charge, status, map_address, map_lat, map_lng)
SELECT user_id, 'Pasta Lane',
  'Wood-fired pizzas, fresh pastas, and classic Italian comfort food.',
  'Italian', '5th Block, Koramangala, Bengaluru 560095', '+91 80 2687 1234', '11:30 AM - 11:00 PM',
  4.30, 40, 5.0, 50.00, 'ACTIVE', '5th Block, Koramangala, Bengaluru 560095', 12.9352, 77.6245
FROM users WHERE email = 'owner3@foodapp.local'
ON DUPLICATE KEY UPDATE description = VALUES(description), status = 'ACTIVE', rating = 4.30;

-- ── Restaurant 4: Dragon Wok ───────────────────────────────
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, contact_phone, opening_hours,
                         rating, delivery_time_minutes, delivery_distance_km, delivery_charge, status, map_address, map_lat, map_lng)
SELECT user_id, 'Dragon Wok',
  'Wok-tossed noodles, fried rice, and bold Chinese flavors made fresh to order.',
  'Chinese', 'ITPL Road, Whitefield, Bengaluru 560066', '+91 80 3012 3456', '12:00 PM - 10:00 PM',
  4.10, 45, 6.8, 50.00, 'ACTIVE', 'ITPL Road, Whitefield, Bengaluru 560066', 12.9698, 77.7500
FROM users WHERE email = 'owner4@foodapp.local'
ON DUPLICATE KEY UPDATE description = VALUES(description), status = 'ACTIVE', rating = 4.10;

-- ── Restaurant 5: Green Bowl (pending approval for demo) ───
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, contact_phone, opening_hours,
                         rating, delivery_time_minutes, delivery_distance_km, delivery_charge, status, map_address)
SELECT user_id, 'Green Bowl',
  'Healthy salads, smoothie bowls, and fresh juices for the health-conscious.',
  'Healthy', 'HSR Layout, Bengaluru 560102', '+91 80 4567 8901', '8:00 AM - 9:00 PM',
  4.00, 35, 4.0, 30.00, 'PENDING', 'HSR Layout, Bengaluru 560102'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE status = 'PENDING';

-- ===== Blue Basil Kitchen categories =====
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Signature Bowls', 1 FROM restaurants WHERE name = 'Blue Basil Kitchen'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Starters', 2 FROM restaurants WHERE name = 'Blue Basil Kitchen'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_items (category_id, name, description, price, discount_price, preparation_time_min, is_veg, availability)
SELECT category_id, 'Paneer Tikka Bowl', 'Charred paneer, fragrant rice, mint yogurt and pickled onions.', 249.00, NULL, 15, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Signature Bowls' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Blue Basil Kitchen')
UNION ALL
SELECT category_id, 'Butter Chicken Bowl', 'Creamy tomato curry, basmati rice and coriander.', 289.00, NULL, 18, FALSE, 'AVAILABLE' FROM menu_categories WHERE name = 'Signature Bowls' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Blue Basil Kitchen')
UNION ALL
SELECT category_id, 'Veg Biryani Bowl', 'Spiced basmati rice with mixed vegetables and saffron.', 219.00, 199.00, 20, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Signature Bowls' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Blue Basil Kitchen')
UNION ALL
SELECT category_id, 'Chilli Paneer', 'Crispy paneer tossed in spicy chilli sauce.', 189.00, NULL, 12, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Starters' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Blue Basil Kitchen')
UNION ALL
SELECT category_id, 'Spring Rolls', 'Golden rolls with vegetable filling.', 149.00, NULL, 10, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Starters' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Blue Basil Kitchen')
ON DUPLICATE KEY UPDATE description = VALUES(description), price = VALUES(price), is_veg = VALUES(is_veg), availability = VALUES(availability);

-- ===== Spice Route categories =====
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Mains', 1 FROM restaurants WHERE name = 'Spice Route'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Breads', 2 FROM restaurants WHERE name = 'Spice Route'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_items (category_id, name, description, price, discount_price, preparation_time_min, is_veg, availability)
SELECT category_id, 'Dal Makhani', 'Slow-cooked black lentils in a creamy tomato gravy.', 259.00, NULL, 20, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Mains' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Spice Route')
UNION ALL
SELECT category_id, 'Kadai Paneer', 'Paneer cooked with bell peppers in a spicy kadai gravy.', 279.00, 249.00, 15, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Mains' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Spice Route')
UNION ALL
SELECT category_id, 'Chole Kulche', 'Punjabi-style chickpea curry served with stuffed bread.', 199.00, NULL, 15, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Mains' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Spice Route')
UNION ALL
SELECT category_id, 'Garlic Naan', 'Soft tandoor-baked bread with garlic and butter.', 59.00, NULL, 8, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Breads' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Spice Route')
UNION ALL
SELECT category_id, 'Tandoori Roti', 'Whole wheat bread baked in a clay oven.', 39.00, NULL, 6, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Breads' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Spice Route')
ON DUPLICATE KEY UPDATE description = VALUES(description), price = VALUES(price), is_veg = VALUES(is_veg), availability = VALUES(availability);

-- ===== Pasta Lane categories =====
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Pasta', 1 FROM restaurants WHERE name = 'Pasta Lane'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Pizza', 2 FROM restaurants WHERE name = 'Pasta Lane'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_items (category_id, name, description, price, discount_price, preparation_time_min, is_veg, availability)
SELECT category_id, 'Margherita Pasta', 'Penne in a fresh basil and tomato sauce.', 229.00, NULL, 15, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Pasta' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Pasta Lane')
UNION ALL
SELECT category_id, 'Alfredo Pasta', 'Creamy white sauce pasta with herbs.', 249.00, NULL, 15, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Pasta' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Pasta Lane')
UNION ALL
SELECT category_id, 'Margherita Pizza', 'Classic pizza with mozzarella and basil.', 299.00, 269.00, 20, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Pizza' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Pasta Lane')
UNION ALL
SELECT category_id, 'Farmhouse Pizza', 'Loaded with bell peppers, onions and mushrooms.', 349.00, NULL, 22, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Pizza' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Pasta Lane')
ON DUPLICATE KEY UPDATE description = VALUES(description), price = VALUES(price), is_veg = VALUES(is_veg), availability = VALUES(availability);

-- ===== Dragon Wok categories =====
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Noodles', 1 FROM restaurants WHERE name = 'Dragon Wok'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Rice', 2 FROM restaurants WHERE name = 'Dragon Wok'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO menu_items (category_id, name, description, price, discount_price, preparation_time_min, is_veg, availability)
SELECT category_id, 'Hakka Noodles', 'Stir-fried noodles with vegetables and soy sauce.', 179.00, NULL, 12, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Noodles' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Dragon Wok')
UNION ALL
SELECT category_id, 'Schezwan Noodles', 'Spicy schezwan sauce noodles with vegetables.', 199.00, NULL, 12, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Noodles' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Dragon Wok')
UNION ALL
SELECT category_id, 'Veg Fried Rice', 'Wok-tossed rice with vegetables and spices.', 169.00, NULL, 12, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Rice' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Dragon Wok')
UNION ALL
SELECT category_id, 'Schezwan Fried Rice', 'Spicy fried rice with schezwan sauce.', 189.00, 169.00, 12, TRUE, 'AVAILABLE' FROM menu_categories WHERE name = 'Rice' AND restaurant_id = (SELECT restaurant_id FROM restaurants WHERE name = 'Dragon Wok')
ON DUPLICATE KEY UPDATE description = VALUES(description), price = VALUES(price), is_veg = VALUES(is_veg), availability = VALUES(availability);

-- ── Inventory for all menu items ───────────────────────────
INSERT INTO inventory (item_id, quantity_available, reorder_level)
SELECT item_id, 50, 10 FROM menu_items
ON DUPLICATE KEY UPDATE quantity_available = GREATEST(quantity_available, 50);

-- Set one item to low stock for demo
UPDATE inventory i JOIN menu_items mi ON mi.item_id = i.item_id
SET i.quantity_available = 3
WHERE mi.name = 'Spring Rolls';

-- ── Default address for demo customer ──────────────────────
INSERT INTO addresses (customer_id, label, address_line, city, postal_code, is_default)
SELECT customer_id, 'Home', '42 Lake View Road', 'Bengaluru', '560001', TRUE FROM customers c
JOIN users u ON u.user_id = c.customer_id WHERE u.email = 'customer@foodapp.local'
AND NOT EXISTS (SELECT 1 FROM addresses a WHERE a.customer_id = c.customer_id);
