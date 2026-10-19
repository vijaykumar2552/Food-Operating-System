-- ============================================================
-- FoodOS Schema — Normalized MySQL for users, roles,
-- restaurants, menu items, customers, orders and order items.
-- ============================================================
CREATE DATABASE IF NOT EXISTS food_ordering CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE food_ordering;

-- ── Users ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
  user_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  email          VARCHAR(150) NOT NULL UNIQUE,
  password_hash  VARCHAR(100) NOT NULL,
  full_name      VARCHAR(120) NOT NULL,
  role           ENUM('CUSTOMER','RESTAURANT_OWNER','RESTAURANT_STAFF','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
  is_active      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ── Customers (1:1 extension of users) ─────────────────────
CREATE TABLE IF NOT EXISTS customers (
  customer_id BIGINT PRIMARY KEY,
  phone       VARCHAR(25),
  FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Restaurants ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS restaurants (
  restaurant_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id           BIGINT NOT NULL,
  name                    VARCHAR(150) NOT NULL,
  description             TEXT,
  cuisine                 VARCHAR(80) NOT NULL,
  location                VARCHAR(180) NOT NULL,
  contact_phone           VARCHAR(25),
  opening_hours           VARCHAR(120),
  rating                  DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  delivery_time_minutes   INT NOT NULL DEFAULT 30,
  delivery_distance_km    DECIMAL(4,1) NOT NULL DEFAULT 0.0,
  delivery_charge         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status                  ENUM('PENDING','ACTIVE','SUSPENDED','CLOSED') NOT NULL DEFAULT 'PENDING',
  cover_image_path        VARCHAR(500),
  map_address             VARCHAR(300),
  map_lat                 DECIMAL(10,7),
  map_lng                 DECIMAL(10,7),
  map_link                VARCHAR(500),
  created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_restaurant_owner_name (owner_user_id, name),
  FOREIGN KEY (owner_user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- ── Menu categories ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_categories (
  category_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  restaurant_id  BIGINT NOT NULL,
  name           VARCHAR(80) NOT NULL,
  sort_order     INT NOT NULL DEFAULT 0,
  UNIQUE KEY uq_category_restaurant_name (restaurant_id, name),
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Menu items ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_items (
  item_id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id           BIGINT NOT NULL,
  name                  VARCHAR(150) NOT NULL,
  description           VARCHAR(500),
  price                 DECIMAL(10,2) NOT NULL,
  discount_price        DECIMAL(10,2),
  preparation_time_min  INT NOT NULL DEFAULT 15,
  is_veg                BOOLEAN NOT NULL DEFAULT TRUE,
  availability          ENUM('AVAILABLE','SOLD_OUT','HIDDEN') NOT NULL DEFAULT 'AVAILABLE',
  image_path            VARCHAR(500),
  created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_menu_item_category_name (category_id, name),
  CHECK (price > 0),
  CHECK (discount_price IS NULL OR discount_price >= 0),
  CHECK (preparation_time_min > 0),
  FOREIGN KEY (category_id) REFERENCES menu_categories(category_id)
) ENGINE=InnoDB;

-- ── Inventory ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory (
  item_id            BIGINT PRIMARY KEY,
  quantity_available INT NOT NULL DEFAULT 0,
  reorder_level      INT NOT NULL DEFAULT 5,
  updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CHECK (quantity_available >= 0),
  FOREIGN KEY (item_id) REFERENCES menu_items(item_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Addresses ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS addresses (
  address_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id  BIGINT NOT NULL,
  label        VARCHAR(40) NOT NULL,
  address_line VARCHAR(255) NOT NULL,
  city         VARCHAR(80) NOT NULL,
  postal_code  VARCHAR(15) NOT NULL,
  is_default   BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Carts ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS carts (
  cart_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL UNIQUE,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cart_items (
  cart_id   BIGINT NOT NULL,
  item_id   BIGINT NOT NULL,
  quantity  INT NOT NULL,
  PRIMARY KEY (cart_id, item_id),
  CHECK (quantity > 0),
  FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
) ENGINE=InnoDB;

-- ── Orders ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
  order_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id     BIGINT NOT NULL,
  restaurant_id   BIGINT NOT NULL,
  address_id      BIGINT NOT NULL,
  status          ENUM('PLACED','ACCEPTED','PREPARING','READY','OUT_FOR_DELIVERY','DELIVERED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PLACED',
  subtotal        DECIMAL(10,2) NOT NULL,
  discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  delivery_fee    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total_amount    DECIMAL(10,2) NOT NULL,
  order_date      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (subtotal >= 0 AND discount_amount >= 0 AND delivery_fee >= 0 AND total_amount >= 0),
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id),
  FOREIGN KEY (address_id) REFERENCES addresses(address_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
  order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id      BIGINT NOT NULL,
  item_id       BIGINT NOT NULL,
  quantity      INT NOT NULL,
  unit_price    DECIMAL(10,2) NOT NULL,
  line_total    DECIMAL(10,2) AS (quantity * unit_price) STORED,
  CHECK (quantity > 0),
  FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
) ENGINE=InnoDB;

-- ── Payments (simulated) ───────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id   BIGINT NOT NULL UNIQUE,
  method     ENUM('CASH','CARD_SIMULATED','UPI_SIMULATED') NOT NULL,
  amount     DECIMAL(10,2) NOT NULL,
  status     ENUM('PENDING','SUCCESS','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
  paid_at    TIMESTAMP NULL,
  FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Reviews ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reviews (
  review_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id       BIGINT NOT NULL UNIQUE,
  customer_id    BIGINT NOT NULL,
  restaurant_id  BIGINT NOT NULL,
  rating         TINYINT NOT NULL,
  comment        VARCHAR(500),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (rating BETWEEN 1 AND 5),
  FOREIGN KEY (order_id) REFERENCES orders(order_id),
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
) ENGINE=InnoDB;

-- ── Order status history ───────────────────────────────────
CREATE TABLE IF NOT EXISTS order_status_history (
  history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id   BIGINT NOT NULL,
  old_status VARCHAR(30),
  new_status VARCHAR(30) NOT NULL,
  changed_by BIGINT,
  changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
  FOREIGN KEY (changed_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- ── Audit logs ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_logs (
  audit_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT,
  action      VARCHAR(80) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id   BIGINT,
  details     JSON,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;
