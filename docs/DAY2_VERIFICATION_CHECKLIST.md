# Day 2 verification checklist

## Restaurant
- [x] Restaurant list loads only ACTIVE restaurants
- [x] Search filters by name and location
- [x] Inactive restaurants excluded by DAO query predicate
- [x] Restaurant details available via active-restaurant lookup

## Menu
- [x] Correct restaurant menu loads with categories
- [x] Menu item name, description, price, availability, and stock shown
- [x] Unavailable items cannot be added to cart (service rejects)
- [x] Price is read from the database, never from the UI

## Cart
- [x] Add item with validated quantity (1–50)
- [x] Increase quantity via change-quantity dialog
- [x] Decrease quantity or remove by setting quantity to 0
- [x] Remove item deletes the cart row
- [x] Clear cart removes all cart items
- [x] Empty cart prevents checkout
- [x] Single-restaurant enforcement
- [x] Subtotal and total quantity computed from DB-authoritative prices

## Checkout
- [x] Successful checkout creates order, order items, payment, and clears cart
- [x] Empty-cart rejection
- [x] Invalid quantity rejection (service bounds 1–50)
- [x] Insufficient stock rejection (guarded UPDATE)
- [x] Order created with DB-authoritative totals
- [x] Order items created with locked unit prices
- [x] Inventory reduced atomically
- [x] Cart finalized after commit

## Transaction failure
- [x] Rollback on any AppException or SQLException
- [x] No partial order (order insert is in the same transaction)
- [x] No partial order items (all inserts before commit)
- [x] No incorrect inventory reduction (guarded UPDATE + rollback)
- [x] Rollback exception suppressed onto primary exception

## Concurrency
- [x] SELECT ... FOR UPDATE locks inventory rows in the transaction
- [x] Rows locked in sorted item-id order to reduce deadlock risk
- [x] Guarded UPDATE prevents overselling if lock is bypassed
- [x] Transaction is short — no Swing input during auto-commit=false

## Security
- [x] All SQL uses PreparedStatement with bound parameters
- [x] No SQL string concatenation with user input
- [x] Prices and inventory are database-authoritative
- [x] Quantity and ID validation in service layer
- [x] Customer session validated via SessionContext
- [x] BCrypt authentication reused from Day 1
- [x] No credentials exposed in UI

## Swing threading
- [x] All database operations run in SwingWorker.doInBackground
- [x] No JDBC calls on the Event Dispatch Thread
- [x] Loading states shown during background work
- [x] Empty states shown when no data returns

## Verification status
- VERIFIED BY SOURCE INSPECTION
- VERIFIED BY DATABASE/SQL CHECK
- NOT VERIFIED — JAVA/MAVEN UNAVAILABLE (Java and Maven not installed in build environment)
