// Renders the FoodOS account page and its route-level interactions.
// TanStack Router composes this React view with shared API and application state.

import { createFileRoute, Link } from "@tanstack/react-router";
import { useAuth } from "@/context/AppProviders";
import { pageMeta } from "@/lib/meta";
import { Button } from "@/components/ui/button";
import { EmptyState, Loading, PageHeader } from "@/components/common/States";

export const Route = createFileRoute("/account")({
  head: () => pageMeta("Account", "Your FoodOS account details."),
  component: Account,
});

function Account() {
  const { user, ready, logout } = useAuth();
  if (!ready) return <Loading />;
  if (!user) return <EmptyState emoji="👋" title="You're not logged in" action={<Button asChild className="rounded-full"><Link to="/login">Login / Register</Link></Button>} />;
  const rows = [["Full name", user.fullName], ["Email", user.email], ["Phone", user.phone], ["Role", user.role]];
  return (
    <div className="mx-auto max-w-xl px-4 py-10">
      <PageHeader title="Account" />
      <div className="rounded-3xl border bg-card p-6 shadow-soft">
        <div className="mb-6 flex items-center gap-4">
          <div className="grid h-16 w-16 place-items-center rounded-full bg-sticker-2 text-3xl">😋</div>
          <div><p className="text-lg font-bold">{user.fullName}</p><p className="text-sm text-muted-foreground">{user.email}</p></div>
        </div>
        <dl className="divide-y">
          {rows.filter(([, v]) => v).map(([k, v]) => (
            <div key={k} className="flex justify-between py-3 text-sm"><dt className="text-muted-foreground">{k}</dt><dd className="font-medium">{v}</dd></div>
          ))}
        </dl>
        <Button variant="outline" className="mt-6 w-full rounded-full" onClick={logout}>Logout</Button>
      </div>
    </div>
  );
}
