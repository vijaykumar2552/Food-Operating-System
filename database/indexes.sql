-- Adds indexes that support common FoodOS catalog and order lookups.
-- Indexed columns align with query filters and relationship access.

USE food_ordering;
CREATE INDEX idx_orders_customer_date ON orders(customer_id, order_date);
CREATE INDEX idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE INDEX idx_orders_status_date ON orders(status, order_date);
CREATE INDEX idx_menu_category_available ON menu_items(category_id, availability);
CREATE INDEX idx_inventory_low_stock ON inventory(quantity_available, reorder_level);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_restaurants_owner_status ON restaurants(owner_user_id, status);
CREATE INDEX idx_restaurants_status ON restaurants(status);
CREATE INDEX idx_menu_items_category_name ON menu_items(category_id, name);
CREATE INDEX idx_users_role ON users(role);
