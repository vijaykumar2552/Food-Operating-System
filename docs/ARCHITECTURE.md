# Architecture

## Request flow

A screen collects input and delegates to a service. The service validates input, controls transactions and calls a DAO. The DAO uses prepared JDBC statements. MySQL enforces relational integrity with keys, checks, indexes and triggers.

## Current vertical slice

`RegisterDialog -> AuthenticationService.register -> JdbcUserDao -> MySQL users/customers/audit_logs`

`LoginFrame -> AuthenticationService.login -> JdbcUserDao -> MySQL users/audit_logs`

`DashboardFrame -> DashboardService -> JDBC aggregate queries -> MySQL`

## Threading

Swing event handlers start `SwingWorker` tasks for authentication and dashboard queries. Results are applied back on the Swing event-dispatch thread. No database query is executed directly while painting or handling the main UI.

## Transaction boundary

Registration inserts a user, a customer profile and an audit record in one transaction. A failure rolls all three changes back. Order placement will use the same connection-scoped pattern with row locks for inventory.

## Implemented Day-2 vertical slices

1. Active restaurant search and selection.
2. Database-backed menu browsing with availability and stock.
3. Single-restaurant customer carts with bounded quantities.
4. Atomic checkout with authoritative pricing and simulated payment.
5. Customer order history and ownership-filtered order details.

## Next vertical slices

1. Restaurant staff order acceptance and status updates.
2. Menu and inventory management.
3. Reviews, admin screens and analytics.
4. Integration tests against a dedicated MySQL test schema.
