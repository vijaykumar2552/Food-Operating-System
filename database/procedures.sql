-- Defines MySQL procedures for FoodOS order history and status updates.
-- Parameterized operations target the requested database records.

USE food_ordering;
DROP PROCEDURE IF EXISTS get_customer_order_history;
DROP PROCEDURE IF EXISTS update_order_status;
DELIMITER $$
CREATE PROCEDURE get_customer_order_history(IN p_customer_id BIGINT)
BEGIN
  SELECT order_id, order_date, status, total_amount FROM orders
  WHERE customer_id = p_customer_id ORDER BY order_date DESC;
END$$
CREATE PROCEDURE update_order_status(IN p_order_id BIGINT, IN p_new_status VARCHAR(30), IN p_actor BIGINT)
BEGIN
  UPDATE orders SET status = p_new_status WHERE order_id = p_order_id;
  INSERT INTO audit_logs(user_id, action, entity_type, entity_id)
  VALUES (p_actor, 'STATUS_CHANGED', 'ORDER', p_order_id);
END$$
DELIMITER ;
