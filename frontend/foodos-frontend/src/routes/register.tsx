// Renders the FoodOS register page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { authService } from "@/services/authService";
import { errorMessage } from "@/services/api";
import { useAuth } from "@/context/AppProviders";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { AuthLayout, Field } from "@/components/common/AuthLayout";

export const Route = createFileRoute("/register")({
  head: () => pageMeta("Create account", "Sign up for FoodOS and start ordering from local restaurants."),
  component: Register,
});

function Register() {
  const { setUser } = useAuth();
  const nav = useNavigate();
  const [f, setF] = useState({ fullName: "", phone: "", email: "", password: "" });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState("");
  const [busy, setBusy] = useState(false);
  const set = (k: keyof typeof f) => (e: React.ChangeEvent<HTMLInputElement>) => setF({ ...f, [k]: e.target.value });

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs: Record<string, string> = {};
    if (f.fullName.trim().length < 2) errs.fullName = "Enter your full name.";
    if (!/^\d{10}$/.test(f.phone.trim())) errs.phone = "Enter a 10-digit phone number.";
    if (!/^\S+@\S+\.\S+$/.test(f.email)) errs.email = "Enter a valid email.";
    if (f.password.length < 8) errs.password = "Password must be at least 8 characters.";
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true);
    setServerError("");
    try {
      await authService.register({ fullName: f.fullName.trim(), phone: f.phone.trim(), email: f.email.trim(), password: f.password });
      nav({ to: "/login" });
    } catch (err) {
      setServerError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <AuthLayout title="Create account" subtitle="Join FoodOS in under a minute.">
      <form onSubmit={submit} className="space-y-4" noValidate>
        <Field label="Full name" autoComplete="name" value={f.fullName} onChange={set("fullName")} error={errors.fullName} />
        <Field label="Phone" type="tel" autoComplete="tel" value={f.phone} onChange={set("phone")} error={errors.phone} />
        <Field label="Email" type="email" autoComplete="email" value={f.email} onChange={set("email")} error={errors.email} />
        <Field label="Password" type="password" autoComplete="new-password" value={f.password} onChange={set("password")} error={errors.password} />
        {serverError && <p className="rounded-xl bg-destructive/10 px-4 py-2 text-sm text-destructive">{serverError}</p>}
        <Button type="submit" size="lg" className="w-full rounded-full" disabled={busy}>{busy ? "Creating account…" : "Register"}</Button>
      </form>
      <p className="mt-6 text-center text-sm text-muted-foreground">Already have an account? <Link to="/login" className="font-semibold text-primary">Log in</Link></p>
    </AuthLayout>
  );
}
