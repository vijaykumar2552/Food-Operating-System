# FoodOS deployment preparation

## Required components

- Java 17 or newer for the backend.
- Maven 3.8 or newer to build the backend.
- MySQL 8 or newer for the `food_ordering` schema.
- Node.js `^20.19.0` or `>=22.12.0` and npm compatible with Vite 8 for the
  nested frontend build.

GitHub stores source code but does not run the Java API or MySQL database. A
deployment needs a Java-capable application service, a managed MySQL 8+
database, and a static frontend host or CDN.

## Configuration

The backend reads `config.properties` from the project root, then environment
fallbacks:

- `db.url` / `DB_URL`
- `db.username` / `DB_USERNAME`
- `db.password` / `DB_PASSWORD`
- `api.port` / `API_PORT`

Keep real values outside Git. Start from
`src/main/resources/config.properties.example` and provide production secrets
through the hosting provider's secret configuration.

The frontend reads:

- `VITE_API_BASE_URL`: deployed HTTPS Java API base URL. Keep `/api` locally so
  the Vite development proxy is used.
- `VITE_USE_MOCK`: must remain unset or `false` for real deployments.

## Build and hosting flow

1. Provision MySQL 8+, create the database/user with least-privilege access,
   and run the SQL files in the order documented in `README.md`.
2. Provision a Java 17+ service, configure the backend secrets, build with
   `mvn clean package`, and run
   `java -cp target/online-food-ordering-system-1.0.0.jar com.foodapp.api.ApiMain`
   on its assigned port. The shaded JAR manifest defaults to the Swing entry
   point, so the API class must be selected explicitly for web hosting.
3. Configure production CORS to allow only the deployed frontend origin. The
   current development handler permits broad CORS and should not be exposed
   unchanged to an untrusted public origin.
4. Build the intended frontend from `frontend/foodos-frontend/` with
   `VITE_API_BASE_URL=https://api.example.com npm run build`.
5. Publish the generated Vite output through a static host/CDN and configure
   history fallback so client-side routes resolve to the frontend entry point.
6. Verify HTTPS, `/api/health`, login, public menu reads, customer checkout,
   and admin authorization from the deployed frontend.

Do not put MySQL credentials, bearer tokens, or local `config.properties` in
frontend files or deployment artifacts.
