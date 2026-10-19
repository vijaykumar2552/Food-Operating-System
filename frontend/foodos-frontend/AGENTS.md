# FoodOS frontend guidance

- Keep API requests in `src/services/` and preserve the Java API's existing
  request and `{ success, data }` response contracts.
- The Java API and MySQL database are authoritative for users, catalog,
  prices, availability, authorization, and orders.
- Real API mode is the default. `VITE_USE_MOCK=true` is an explicit preview
  option; a failed API request must never fall back to mock data.
- Keep React route components in `src/routes/`, reusable UI in `src/components/`,
  shared state in `src/context/`, and API shapes in `src/types/`.
- Preserve the `/api` Vite proxy to `http://localhost:8080`.
- Do not edit generated route-tree files directly; regenerate them through the
  existing TanStack/Vite tooling.
