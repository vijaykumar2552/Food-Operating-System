# Day 2 ordering flow

```text
Customer
  ↓
Swing UI (OrderingFrame)
  ↓
Service Layer (Restaurant/Menu/Cart/Order)
  ↓
DAO Layer
  ↓
JDBC transaction
  ↓
MySQL
  ↓
Orders + Order Items + Inventory
```

## Restaurant and menu

Only `ACTIVE` restaurants are returned. Restaurant search uses bound parameters for name and location. Menu queries join categories and inventory, so displayed prices and stock are read from MySQL rather than stored in the interface.

## Cart

The cart is persisted in `carts` and `cart_items`, keyed by the signed-in customer. A cart may contain one restaurant at a time. Quantities are restricted to 1–50 and the database check prevents non-positive rows.

## Checkout transaction

`OrderService.checkout` opens one JDBC connection, disables auto-commit, reads the cart, locks every inventory row in sorted item-id order with `SELECT ... FOR UPDATE`, validates availability and stock, reduces inventory with a guarded update, computes totals, creates the address/order/order items/payment, clears the cart, and commits. Any application or SQL failure rolls back the transaction.

Prices are read again during checkout. The UI cannot set the order price or inventory result.

## Order lifecycle

The existing database lifecycle is reused: `PLACED → ACCEPTED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED`, with `REJECTED` and `CANCELLED` available for later staff workflows. Customer history and details are ownership-filtered by customer id in the DAO query.

## Verification status

- Source inspection: completed for session integration, prepared statements, SwingWorker use, transaction boundary, rollback path, and inventory lock.
- SQL inspection: completed for cart foreign keys, uniqueness, quantity checks, and InnoDB tables.
- Automated Java/Maven execution: not available in the build environment because Java and Maven are not installed.
- Live MySQL execution: not performed in this environment.
