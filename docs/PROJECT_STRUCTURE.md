# FoodOS project structure

## Runtime flow

The intended web application is the Vite/TanStack frontend:

`frontend/foodos-frontend/src`
`→ services/api.ts → HTTP /api → ApiHandler → service → DAO/JDBC → MySQL`

The project root also contains a separate React 18/Vite 5 frontend
(`package.json`, root `src/`, and root `vite.config.ts`). Its proxy also targets
the local API, while its `src/lib/db.ts` module also provides a browser
IndexedDB adapter for that client. These are distinct applications; the
frontend under `frontend/foodos-frontend/` is the intended web client and uses
the Java API as the source of business data.

The frontend stores only the bearer token and display-safe user information in
browser storage. The Java API owns authentication, authorization, validation,
prices, inventory, totals, and order persistence.

## Backend

- `src/main/java/com/foodapp/api/` contains `ApiMain`, `ApiHandler`, and
  `SessionStore`. `ApiHandler` maps HTTP requests to application services and
  applies customer/admin authorization.
- `src/main/java/com/foodapp/service/` contains business rules such as
  `AuthenticationService`, `CartService`, `OrderService`, and
  `RestaurantService`.
- `src/main/java/com/foodapp/dao/` contains DAO interfaces and JDBC
  implementations such as `JdbcUserDao`, `JdbcCartDao`, and `JdbcOrderDao`.
- `src/main/java/com/foodapp/model/` contains immutable records, entities, and
  enums used across the service and DAO layers.
- `src/main/java/com/foodapp/config/` loads `config.properties` or environment
  fallbacks through `AppConfig`.
- `src/main/java/com/foodapp/util/` contains shared database/session utilities.
- `src/main/java/com/foodapp/exception/` contains typed application errors.
- `src/test/java/` contains unit tests that do not require production data.

`OrderService.checkout` uses one JDBC connection, row locks for inventory,
database prices, and commit/rollback semantics. `JdbcOrderDao` persists the
order header, order items, and simulated COD payment.

## Frontend

- `src/routes/` contains TanStack Router route components, including
  `checkout.tsx`, `orders.tsx`, and admin screens.
- `src/components/` contains reusable UI components.
- `src/services/` contains typed API-facing services.
- `src/context/AppProviders.tsx` contains authentication display state and the
  browser cart state.
- `src/lib/` contains pure helpers and focused tests.
- `src/types/` contains shared TypeScript request and response types.
- `src/mock/` is available only when `VITE_USE_MOCK=true`; real API mode is the
  default.
- Vite 8 requires Node.js `^20.19.0` or `>=22.12.0`; use one of those supported
  Node releases for the nested frontend's build and test commands.

## OOP concepts actually used

- **Classes and objects:** services, DAOs, API handlers, Swing views, and model
  objects are instantiated and collaborate through constructors.
- **Encapsulation:** fields and configuration state are kept inside classes;
  public methods expose operations such as `OrderService.checkout`.
- **Abstraction:** `UserDao`, `CartDao`, `OrderDao`, and related interfaces
  separate persistence contracts from JDBC implementations.
- **Polymorphism:** code depends on DAO interface types while using concrete
  `Jdbc*Dao` implementations.
- **Inheritance:** application exceptions extend `AppException`; specialized
  authentication, order, and database exceptions inherit its behavior.
- **Overloading:** `OrderService.checkout` supports checkout from the persisted
  cart or an explicit validated item list.
- **Generics and collections:** Java lists, sets, maps, and typed JDBC/API
  collections are used throughout validation and response construction.
- **Exception handling:** API boundaries map typed failures to safe HTTP
  responses while server logs retain diagnostic exception information.
- **JDBC transactions:** registration and checkout use explicit commit/rollback
  boundaries and prepared statements.

## Database

`database/schema.sql` creates the normalized users, customers, restaurants,
menus, inventory, carts, orders, payments, and related tables. The remaining
SQL files add indexes, routines, views, triggers, migrations, and additive
sample data. Run them in the order documented in `README.md`.
