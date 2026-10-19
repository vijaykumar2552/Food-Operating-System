// Provides reusable FoodOS frontend helpers for utils behavior.
// Keeps shared presentation or data logic outside route components.

import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
