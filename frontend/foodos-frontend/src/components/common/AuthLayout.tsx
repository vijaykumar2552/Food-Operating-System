// Implements the reusable FoodOS auth layout interface component.
// React props and event handlers connect this UI element to its parent workflow.

import type { ReactNode } from "react";

export function AuthLayout({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  return (
    <div className="bg-hero">
      <div className="mx-auto grid max-w-5xl items-center gap-10 px-4 py-16 md:grid-cols-2">
        <div className="hidden text-center md:block">
          <div className="flex justify-center gap-3 text-7xl">
            <span className="sticker float-slow">🍕</span>
            <span className="sticker float-slow [animation-delay:-1.5s]">🍛</span>
            <span className="sticker float-slow [animation-delay:-3s]">🧁</span>
          </div>
          <p className="mt-6 font-display text-3xl font-extrabold">Good food.<br /><span className="text-primary">Smarter choices.</span></p>
        </div>
        <div className="rounded-3xl border bg-card p-8 shadow-lift">
          <h1 className="text-3xl font-extrabold">{title}</h1>
          <p className="mb-6 mt-1 text-muted-foreground">{subtitle}</p>
          {children}
        </div>
      </div>
    </div>
  );
}

export function Field({ label, error, ...props }: React.InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium">{label}</span>
      <input {...props} className={`w-full rounded-xl border bg-background px-4 py-2.5 outline-none transition-shadow focus:ring-2 focus:ring-ring/40 ${error ? "border-destructive" : ""}`} />
      {error && <span className="mt-1 block text-xs text-destructive">{error}</span>}
    </label>
  );
}
