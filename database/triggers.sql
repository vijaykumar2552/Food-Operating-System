-- Records FoodOS order status changes through MySQL triggers.
-- Trigger actions run automatically when associated order rows change.

USE food_ordering;
DROP TRIGGER IF EXISTS trg_orders_after_insert;
DROP TRIGGER IF EXISTS trg_orders_after_update;
DELIMITER $$
CREATE TRIGGER trg_orders_after_insert AFTER INSERT ON orders FOR EACH ROW
BEGIN
  INSERT INTO order_status_history(order_id, old_status, new_status) VALUES (NEW.order_id, NULL, NEW.status);
END$$
CREATE TRIGGER trg_orders_after_update AFTER UPDATE ON orders FOR EACH ROW
BEGIN
  IF OLD.status <> NEW.status THEN
    INSERT INTO order_status_history(order_id, old_status, new_status) VALUES (NEW.order_id, OLD.status, NEW.status);
  END IF;
END$$
DELIMITER ;
