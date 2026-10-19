# FoodOS — Food Ordering & Optimization System

FoodOS is a Java 17, JDBC, and MySQL application with two separately maintained
React web clients. The intended customer and admin client is React 19,
TypeScript, Vite 8, and TanStack Router under `frontend/foodos-frontend/`.
The project-root frontend is a separate React 18/Vite 5 application.

`React UI -> API service -> Java API -> Service -> DAO -> JDBC -> MySQL`

## Project layout

- `src/main/java/` — Java API, services, JDBC DAOs, models, and desktop UI.
- `src/test/java/` — Java unit tests.
- `database/` — MySQL schema, migrations, routines, and sample data scripts.
- `frontend/foodos-frontend/` — intended FoodOS customer/admin web client.
- `src/`, root `package.json`, and `vite.config.ts` — separate React 18 client.
- `docs/` — deployment and architecture documentation.

## Features

### Customer experience
- Role-based login (Customer / Restaurant Staff / Admin)
- Customer registration with atomic user + profile creation
- Dashboard with live stats (active restaurants, active orders, recent orders)
- Browse active restaurants with search
- View restaurant menus with prices, availability, and stock levels
- Add items to cart with quantity validation
- Web cart stored in browser state; the Java API revalidates submitted item IDs and quantities
- Change quantity, remove items, clear cart
- Checkout with delivery address, payment method selection, and order confirmation
- Order history with status badges
- Order details dialog with status timeline and itemized breakdown
- Settings page with profile information

### Staff / Admin experience
- Staff dashboard with pending/preparing/completed order counts and today's revenue
- Low-stock alerts panel
- Menu management (toggle availability, update prices)
- Inventory management (update stock, mark available/unavailable)
- Analytics (total orders, completed orders, revenue, average order value, status distribution, top-selling items)
- Notifications panel

### Technical features
- BCrypt password hashing
- Transactional checkout with `SELECT ... FOR UPDATE` inventory locking
- Guarded inventory reduction (no overselling)
- Rollback on any failure
- `SwingWorker` for database operations in the desktop UI
- `PreparedStatement` throughout (SQL injection safe)
- Database-authoritative prices and inventory
- Reusable UI component system (stat cards, status badges, empty states, loading states)
- Professional sidebar navigation with active-section highlighting

## Setup (Windows)

### Prerequisites
1. **Java 17+** - download from https://adoptium.net (Temurin 17 LTS). Install and verify: `java -version`
2. **Maven 3.8+** - download from https://maven.apache.org/download.cgi. Extract, add `bin` folder to PATH. Verify: `mvn -version`
3. **MySQL 8+** - download from https://dev.mysql.com/downloads/installer/.
4. **Node.js `^20.19.0` or `>=22.12.0` and npm** for the intended Vite 8 frontend.

### Step 1 - Create the database and user
Open MySQL Command Line or MySQL Workbench and run:
```sql
CREATE DATABASE IF NOT EXISTS food_ordering CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'foodapp'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON food_ordering.* TO 'foodapp'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2 - Load the schema and seed data
In MySQL Command Line or Workbench, run these files in order:
```
database/schema.sql
database/indexes.sql
database/functions.sql
database/triggers.sql
database/procedures.sql
database/views.sql
database/seed.sql
database/sample_catalog.sql
```
If upgrading from a Day-1-only database, run `database/day2_cart_migration.sql` before `seed.sql`.

### Step 3 - Configure the database connection
Copy `src/main/resources/config.properties.example` to a file named `config.properties` in the project root folder (same folder as `pom.xml`). Edit it:
```
db.url=jdbc:mysql://localhost:3306/food_ordering?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
db.username=foodapp
db.password=your_password
```

### Step 4 - Build and run the backend
Open a terminal (Command Prompt or PowerShell) in the project folder:
```
mvn clean package
java -jar target/online-food-ordering-system-1.0.0.jar
```

The shaded JAR's manifest starts the Swing application. To run the web API
from the shaded JAR instead, invoke its API main class explicitly:

```
java -cp target/online-food-ordering-system-1.0.0.jar com.foodapp.api.ApiMain
```

The API listens on port `8080` by default; set `api.port` in
`config.properties` or `API_PORT` to change it.

### Step 5 - Run the intended web frontend

Open a second terminal in:
```
frontend/foodos-frontend
```

Run:
```
npm install
npm run dev
```
If you use Bun, run `bun install` in place of `npm install`.

The safe local template is `frontend/foodos-frontend/.env.example`; copy it to
`.env` if you need to override the defaults. The Vite development proxy
forwards `/api` to `http://localhost:8080`. Real API mode is the default;
mock data is an explicit development option only.
For deployment, set `VITE_API_BASE_URL` to the deployed HTTPS API URL before
running `npm run build`; production must not depend on localhost.

### Alternate root frontend

The separate React 18 client can be run from the project root with
`npm install` followed by `npm run dev`. Its development server also proxies
`/api` to `http://localhost:8080`. Use the nested FoodOS frontend above for the
intended customer/admin experience.

The API provides `GET /api/health`, public restaurant and menu reads,
`POST /api/auth/register`, `POST /api/auth/login`, customer
`GET /api/orders/my`, customer `POST /api/orders` (Cash on Delivery only), and
Bearer-token-protected `/api/admin` restaurant, menu-item, and order-status
operations. React checkout sends only menu-item IDs and quantities; the API
re-reads availability, inventory, discounts, and delivery charges from MySQL
inside the checkout transaction. A simple manual check is: call `/api/health`,
register or log in a customer, verify public restaurant/menu reads, verify
`/api/orders/my` rejects anonymous requests, and verify an Admin token is
required for `/api/admin`.

### API contract

JSON responses use `{ "success": true|false, "data": ... }`; no-content
operations such as logout and successful deletes return HTTP 204 without a
body. Errors use an appropriate HTTP status and keep database details server-side:
`{ "success": false, "data": { "error": "..." } }`.

| Method and path | Authentication | Request/body and result |
|---|---|---|
| `GET /api/restaurants` | None | Optional `search` and `cuisine`; returns active restaurants. |
| `GET /api/restaurants/{id}/menu` | None | Optional `search` and `category`; returns all available menu records for that restaurant. |
| `POST /api/auth/register` | None | `{fullName,email,password,phone}`; creates a `CUSTOMER`. |
| `POST /api/auth/login` | None | `{email,password}`; returns a bearer `token` and user. |
| `POST /api/auth/logout` | Bearer token | Revokes the current session token. |
| `GET /api/orders/my` | Customer | Returns only the authenticated customer's order history. |
| `POST /api/orders` | Customer | `{items:[{itemId,quantity}],addressLine,city,postalCode,paymentMethod:"CASH"}`; prices, availability, restaurant, totals, payment, and inventory are checked and saved transactionally. |
| `GET /api/admin/restaurants` | Admin | Returns all restaurants, including status and delivery fields. |
| `POST /api/admin/restaurants` | Admin | `{name,cuisine,location,description?,contactPhone?,openingHours?,status?}`. |
| `PUT /api/admin/restaurants/{id}` | Admin | Partial update of supported restaurant fields. |
| `GET /api/admin/menu-items?restaurantId={id}` | Admin | Returns the complete database-backed menu for one restaurant. |
| `POST /api/admin/menu-items` | Admin | `{categoryId,name,price,description?,discountPrice?,preparationTimeMinutes?,isVeg?,availability?}`; category ownership and inventory creation are checked transactionally. |
| `PUT /api/admin/menu-items/{id}` | Admin | Partial menu update; a replacement category must belong to the same restaurant. |
| `DELETE /api/admin/menu-items/{id}` | Admin | Deletes the menu item when database relationships allow it. |
| `GET /api/admin/dashboard` | Admin | Returns restaurant count, menu-item count, order count, non-cancelled/non-rejected revenue, and top five popular items from persisted records. This is revenue, not profit. |
| `GET /api/admin/orders` | Admin | Returns persisted orders for administration. |
| `PUT /api/admin/orders/{id}` | Admin | `{status}`; only valid order-status transitions are accepted. |

Send `Authorization: Bearer <session-token>` for protected calls. MySQL credentials
remain server-side in local configuration or environment variables and must
never be placed in the frontend.

## Local account setup

Do not commit or document passwords. Create a customer through the registration
screen or `POST /api/auth/register`. Provision administrators separately using
the existing secure BCrypt/password administration procedure. Existing database
contents may differ from the sample seed.

## Project and request flow

See [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) for the backend and frontend layout, request flow, and verified OOP concepts. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for existing architecture notes.

## Tests and builds

Run the intended frontend checks from `frontend/foodos-frontend/`:

```
npm.cmd test -- --run
npm.cmd run build
```

Build the separate root frontend from the project root:

```
npm.cmd run build
```

Run Java tests and build the backend from the project root, with Maven installed:

```
mvn test
mvn clean package
```

Do not treat an unavailable Maven command as a passing build.

## Deployment

Read [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) before hosting. GitHub is source hosting only; deploy the Java API, MySQL database, and static frontend to suitable separate services. Configure the frontend with the deployed HTTPS API URL using `VITE_API_BASE_URL`; never include backend credentials in Vite variables.

## Security notes

- Keep `config.properties`, `.env` secrets, tokens, and credentials out of Git.
- Database access is configured server-side using `config.properties` or `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `API_PORT`.
- API sessions are bearer-token based and held in server memory; restarting the API invalidates active sessions.
- Production hosting must restrict CORS to the deployed frontend origin; the current API CORS behavior is permissive and needs hardening before public exposure.
- Payments are simulated Cash on Delivery; no real payment gateway is integrated.