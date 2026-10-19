// Renders the FoodOS login page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { authService } from "@/services/authService";
import { USE_MOCK, errorMessage } from "@/services/api";
import { useAuth } from "@/context/AppProviders";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { AuthLayout, Field } from "@/components/common/AuthLayout";

export const Route = createFileRoute("/login")({
  head: () => pageMeta("Login", "Log in to FoodOS to order food and track deliveries."),
  component: Login,
});

function Login() {
  const { setUser } = useAuth();
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs: Record<string, string> = {};
    if (!/^\S+@\S+\.\S+$/.test(email)) errs.email = "Enter a valid email.";
    if (!password) errs.password = "Enter your password.";
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true);
    setServerError("");
    try {
      const user = await authService.login({ email: email.trim(), password });
      setUser(user);
      nav({ to: user.role === "ADMIN" ? "/admin" : "/" });
    } catch (err) {
      setServerError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <AuthLayout title="Welcome back" subtitle="Log in to continue ordering.">
      <form onSubmit={submit} className="space-y-4" noValidate>
        <Field label="Email" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} error={errors.email} />
        <Field label="Password" type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} error={errors.password} />
        {serverError && <p className="rounded-xl bg-destructive/10 px-4 py-2 text-sm text-destructive">{serverError}</p>}
        <Button type="submit" size="lg" className="w-full rounded-full" disabled={busy}>{busy ? "Logging in…" : "Login"}</Button>
      </form>
      <p className="mt-6 text-center text-sm text-muted-foreground">New to FoodOS? <Link to="/register" className="font-semibold text-primary">Create an account</Link></p>
      {USE_MOCK && (
        <p className="mt-4 rounded-xl bg-muted p-3 text-xs text-muted-foreground">
          Preview demo accounts (sample data only): customer@demo.foodos or admin@demo.foodos, password demo1234.
        </p>
      )}
    </AuthLayout>
  );
}
