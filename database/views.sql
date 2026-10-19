-- Defines database views for FoodOS reporting and read models.
-- Views present derived data without duplicating stored records.

USE food_ordering;
CREATE OR REPLACE VIEW CustomerOrderSummary AS
SELECT o.customer_id, u.full_name, COUNT(o.order_id) AS order_count,
       COALESCE(SUM(o.total_amount), 0) AS total_spent,
       MAX(o.order_date) AS last_order_date
FROM orders o JOIN customers c ON c.customer_id = o.customer_id
JOIN users u ON u.user_id = c.customer_id
GROUP BY o.customer_id, u.full_name;

CREATE OR REPLACE VIEW RestaurantRevenueSummary AS
SELECT r.restaurant_id, r.name, COUNT(o.order_id) AS order_count,
       COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' THEN o.total_amount ELSE 0 END), 0) AS revenue
FROM restaurants r LEFT JOIN orders o ON o.restaurant_id = r.restaurant_id
GROUP BY r.restaurant_id, r.name;

CREATE OR REPLACE VIEW PopularMenuItems AS
SELECT mi.item_id, mi.name, r.name AS restaurant_name,
       COALESCE(SUM(oi.quantity), 0) AS units_sold
FROM menu_items mi JOIN menu_categories mc ON mc.category_id = mi.category_id
JOIN restaurants r ON r.restaurant_id = mc.restaurant_id
LEFT JOIN order_items oi ON oi.item_id = mi.item_id
GROUP BY mi.item_id, mi.name, r.name;

CREATE OR REPLACE VIEW CustomerSpendingSummary AS
SELECT customer_id, COUNT(*) AS delivered_orders, SUM(total_amount) AS total_spent,
       AVG(total_amount) AS average_order_value
FROM orders WHERE status = 'DELIVERED' GROUP BY customer_id;
