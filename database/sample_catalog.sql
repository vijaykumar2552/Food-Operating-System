-- Adds or updates the screenshot-aligned FoodOS sample catalog.
-- Idempotent key-based inserts preserve unrelated restaurant and order data.

USE food_ordering;

-- Screenshot-aligned sample catalog. Safe to rerun; it updates matching
-- owner/name, category/name, and item/category records without deleting data.

INSERT INTO users (email, password_hash, full_name, role)
VALUES ('admin@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Admin', 'ADMIN')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), role=VALUES(role);

INSERT INTO users (email, password_hash, full_name, role)
VALUES ('owner1@foodapp.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'FoodOS Sample Owner', 'RESTAURANT_OWNER')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), role=VALUES(role);

INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, delivery_charge, status)
SELECT user_id, 'Slice Republic', 'Wood-fired pizzas and creamy pastas.', 'Italian', 'MG Road', 40.00, 'ACTIVE'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE cuisine=VALUES(cuisine), location=VALUES(location), delivery_charge=VALUES(delivery_charge), status=VALUES(status), description=VALUES(description);
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, delivery_charge, status)
SELECT user_id, 'Biryani Junction', 'Slow-cooked dum biryani and kebabs.', 'Biryani', 'Koramangala', 30.00, 'ACTIVE'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE cuisine=VALUES(cuisine), location=VALUES(location), delivery_charge=VALUES(delivery_charge), status=VALUES(status), description=VALUES(description);
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, delivery_charge, status)
SELECT user_id, 'Burger Barn', 'Smash burgers, loaded fries and shakes.', 'American', 'Indiranagar', 35.00, 'ACTIVE'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE cuisine=VALUES(cuisine), location=VALUES(location), delivery_charge=VALUES(delivery_charge), status=VALUES(status), description=VALUES(description);
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, delivery_charge, status)
SELECT user_id, 'Wok This Way', 'Wok-tossed noodles and dumplings.', 'Chinese', 'HSR Layout', 25.00, 'CLOSED'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE cuisine=VALUES(cuisine), location=VALUES(location), delivery_charge=VALUES(delivery_charge), status=VALUES(status), description=VALUES(description);
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, delivery_charge, status)
SELECT user_id, 'Green Bowl', 'Fresh salads, wraps and smoothie bowls.', 'Healthy', 'Whitefield', 0.00, 'ACTIVE'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE cuisine=VALUES(cuisine), location=VALUES(location), delivery_charge=VALUES(delivery_charge), status=VALUES(status), description=VALUES(description);
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, delivery_charge, status)
SELECT user_id, 'Sweet Tooth', 'Cakes, brownies and ice cream sundaes.', 'Desserts', 'Jayanagar', 20.00, 'ACTIVE'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE cuisine=VALUES(cuisine), location=VALUES(location), delivery_charge=VALUES(delivery_charge), status=VALUES(status), description=VALUES(description);
INSERT INTO restaurants (owner_user_id, name, description, cuisine, location, delivery_charge, status)
SELECT user_id, 'My Friends restaurants', 'South Indian favourites for sharing.', 'South Indian', 'Suchitra Circle', 20.00, 'ACTIVE'
FROM users WHERE email = 'owner1@foodapp.local'
ON DUPLICATE KEY UPDATE cuisine=VALUES(cuisine), location=VALUES(location), delivery_charge=VALUES(delivery_charge), status=VALUES(status), description=VALUES(description);

INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Pizza', 1 FROM restaurants WHERE name='Slice Republic'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Pasta', 2 FROM restaurants WHERE name='Slice Republic'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Sides', 3 FROM restaurants WHERE name='Slice Republic'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Biryani', 1 FROM restaurants WHERE name='Biryani Junction'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Starters', 2 FROM restaurants WHERE name='Biryani Junction'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Burgers', 1 FROM restaurants WHERE name='Burger Barn'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Sides', 2 FROM restaurants WHERE name='Burger Barn'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Drinks', 3 FROM restaurants WHERE name='Burger Barn'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Noodles', 1 FROM restaurants WHERE name='Wok This Way'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Starters', 2 FROM restaurants WHERE name='Wok This Way'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Salads', 1 FROM restaurants WHERE name='Green Bowl'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Sandwiches', 2 FROM restaurants WHERE name='Green Bowl'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'Desserts', 1 FROM restaurants WHERE name='Sweet Tooth'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);
INSERT INTO menu_categories (restaurant_id, name, sort_order)
SELECT restaurant_id, 'South Indian', 1 FROM restaurants WHERE name='My Friends restaurants'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);

INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Margherita Pizza','Tomato, mozzarella, basil',249.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Pizza' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Slice Republic')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Farmhouse Pizza','Capsicum, onion, mushroom, corn',329.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Pizza' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Slice Republic')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Alfredo Pasta','Creamy white sauce penne',279.00,TRUE,'SOLD_OUT' FROM menu_categories WHERE name='Pasta' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Slice Republic')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Garlic Bread','Buttery, cheesy, golden',129.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Sides' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Slice Republic')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);

INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Chicken Dum Biryani','Basmati rice, tender chicken, saffron',299.00,FALSE,'AVAILABLE' FROM menu_categories WHERE name='Biryani' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Biryani Junction')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Veg Biryani','Seasonal vegetables, fragrant spices',229.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Biryani' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Biryani Junction')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Seekh Kebab','Charcoal-grilled minced kebabs',249.00,FALSE,'AVAILABLE' FROM menu_categories WHERE name='Starters' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Biryani Junction')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);

INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Classic Smash Burger','Double patty, cheddar, pickles',219.00,FALSE,'AVAILABLE' FROM menu_categories WHERE name='Burgers' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Burger Barn')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Loaded Fries','Cheese sauce, jalapeños',149.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Sides' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Burger Barn')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Chocolate Shake','Thick and creamy',159.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Drinks' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Burger Barn')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);

INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Hakka Noodles','Wok-tossed with veggies',189.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Noodles' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Wok This Way')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Steamed Momos','Six dumplings with chilli dip',149.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Starters' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Wok This Way')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);

INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Greek Salad','Feta, olives, cucumber',199.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Salads' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Green Bowl')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Paneer Wrap','Grilled paneer, mint mayo',179.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Sandwiches' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Green Bowl')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);

INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Chocolate Truffle Cake','Rich slice, ganache',169.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Desserts' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Sweet Tooth')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Ice Cream Sundae',NULL,189.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='Desserts' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='Sweet Tooth')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);

-- Clearly separate reasonable additions for the seventh restaurant.
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Masala Dosa','Crisp dosa with potato masala and chutney',149.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='South Indian' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='My Friends restaurants')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);
INSERT INTO menu_items (category_id,name,description,price,is_veg,availability)
SELECT category_id,'Idli Sambar','Steamed idlis with sambar',99.00,TRUE,'AVAILABLE' FROM menu_categories WHERE name='South Indian' AND restaurant_id=(SELECT restaurant_id FROM restaurants WHERE name='My Friends restaurants')
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),availability=VALUES(availability);

INSERT INTO inventory (item_id,quantity_available,reorder_level)
SELECT item_id,100,5 FROM menu_items
WHERE name IN ('Margherita Pizza','Farmhouse Pizza','Alfredo Pasta','Garlic Bread','Chicken Dum Biryani','Veg Biryani','Seekh Kebab','Classic Smash Burger','Loaded Fries','Chocolate Shake','Hakka Noodles','Steamed Momos','Greek Salad','Paneer Wrap','Chocolate Truffle Cake','Ice Cream Sundae','Masala Dosa','Idli Sambar')
ON DUPLICATE KEY UPDATE reorder_level=VALUES(reorder_level);
