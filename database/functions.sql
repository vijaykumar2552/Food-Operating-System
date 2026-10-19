-- Defines MySQL functions used to calculate FoodOS order amounts.
-- Stored routines keep database calculations reusable by SQL callers.

USE food_ordering;
DROP FUNCTION IF EXISTS calculate_delivery_fee;
DROP FUNCTION IF EXISTS calculate_discount;
DROP FUNCTION IF EXISTS calculate_order_total;

DELIMITER $$
CREATE FUNCTION calculate_delivery_fee(p_subtotal DECIMAL(10,2)) RETURNS DECIMAL(10,2) DETERMINISTIC
BEGIN
  RETURN CASE WHEN p_subtotal >= 500 THEN 0.00 ELSE 40.00 END;
END$$

CREATE FUNCTION calculate_discount(p_subtotal DECIMAL(10,2)) RETURNS DECIMAL(10,2) DETERMINISTIC
BEGIN
  RETURN CASE WHEN p_subtotal >= 1000 THEN ROUND(p_subtotal * 0.10, 2) ELSE 0.00 END;
END$$

CREATE FUNCTION calculate_order_total(p_subtotal DECIMAL(10,2)) RETURNS DECIMAL(10,2) DETERMINISTIC
BEGIN
  RETURN ROUND(p_subtotal - calculate_discount(p_subtotal) + calculate_delivery_fee(p_subtotal), 2);
END$$
DELIMITER ;
