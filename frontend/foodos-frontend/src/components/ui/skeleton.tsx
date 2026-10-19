// Provides a reusable React UI primitive for FoodOS skeleton controls.
// Typed props and accessible interactions let pages compose a consistent interface.

import { cn } from "@/lib/utils";

function Skeleton({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("animate-pulse rounded-md bg-primary/10", className)} {...props} />;
}

export { Skeleton };
