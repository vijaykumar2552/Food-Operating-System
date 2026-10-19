// The TanStack Vite preset already includes these plugins and runtime settings;
// adding them again would duplicate plugin registration:
//   - TanStack devtools (dev-only, first), tanstackStart, viteReact, tailwindcss, tsConfigPaths,
//     nitro (build-only using cloudflare as a default target), VITE_* env injection, @ path alias,
//     React/TanStack dedupe, error logger plugins, and sandbox detection (port/host/strictPort).
// Add FoodOS-specific Vite options through the exported defineConfig call below.
import { defineConfig } from "@lovable.dev/vite-tanstack-config";

export default defineConfig({
  tanstackStart: {
    server: { entry: "server" },
  },
  vite: {
    server: {
      // Forward /api calls to the Java backend during local development.
      proxy: {
        "/api": { target: "http://localhost:8080", changeOrigin: true },
      },
    },
  },
});
