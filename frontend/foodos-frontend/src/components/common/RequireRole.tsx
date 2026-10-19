// Implements the reusable FoodOS require role interface component.
// React props and event handlers connect this UI element to its parent workflow.

import type { ReactNode } from "react";
import { Link } from "@tanstack/react-router";
import type { Role } from "@/types";
import { useAuth } from "@/context/AppProviders";
import { Button } from "@/components/ui/button";
import { EmptyState, Loading } from "./States";

// UI gate only. Real authorization is enforced by the Java backend.
export function RequireRole({ role, children }: { role: Role; children: ReactNode }) {
  const { user, ready } = useAuth();
  if (!ready) return <Loading />;
  if (!user) return <EmptyState emoji="🔐" title="Please log in" action={<Button asChild className="rounded-full"><Link to="/login">Login</Link></Button>} />;
  if (user.role !== role) return <EmptyState emoji="🚫" title="This page isn't available for your account" action={<Button asChild variant="outline" className="rounded-full"><Link to="/">Go home</Link></Button>} />;
  return <>{children}</>;
}
