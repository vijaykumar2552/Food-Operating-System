# ER model

```text
users 1--0..1 customers
users 1--N restaurants
restaurants 1--N menu_categories 1--N menu_items 1--1 inventory
customers 1--N addresses
customers 1--N orders N--1 restaurants
orders 1--N order_items N--1 menu_items
orders 1--1 payments
orders 1--N order_status_history
customers 1--N reviews N--1 restaurants
users 1--N audit_logs
```

`order_items.unit_price` is intentionally stored as a snapshot. Menu prices can change later without changing historical orders.
