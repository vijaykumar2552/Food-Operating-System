// Implements the reusable FoodOS sticker interface component.
// React props and event handlers connect this UI element to its parent workflow.

import { cn } from "@/lib/utils";
import { stickerTone } from "@/lib/food";

interface Props {
  emoji: string;
  seed?: string | number;
  size?: "sm" | "md" | "lg";
  className?: string;
}

const SIZES = { sm: "h-14 w-14 text-3xl", md: "h-24 w-24 text-5xl", lg: "h-32 w-32 text-7xl" };

export function Sticker({ emoji, seed = emoji, size = "md", className }: Props) {
  return (
    <div className={cn("relative grid shrink-0 place-items-center rounded-[38%] rotate-[-4deg]", stickerTone(seed), SIZES[size], className)}>
      <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-card/70" />
      <span className="absolute bottom-3 left-2 h-1.5 w-1.5 rounded-full bg-card/60" />
      <span className="sticker select-none leading-none" aria-hidden>
        {emoji}
      </span>
    </div>
  );
}
